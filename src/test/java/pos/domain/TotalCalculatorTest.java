package pos.domain;

import org.junit.jupiter.api.Test;
import pos.domain.exception.DomainException;
import pos.domain.model.*;
import pos.domain.service.GradedCriterion;
import pos.domain.service.TotalCalculator;
import pos.domain.service.TotalScoreStrategy;
import pos.domain.service.strategy.NormalizedToTenStrategy;
import pos.domain.service.strategy.WeightedSumStrategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TotalCalculatorTest {

    private final StudentId student = new StudentId("231001");

    private Rubric rubric(double examMax) {
        Rubric r = new Rubric();
        r.addCriterion(new CriterionId("HW"), "Домашние задания", 10, new Weight(0.4));
        r.addCriterion(new CriterionId("EXAM"), "Экзамен", examMax, new Weight(0.6));
        return r;
    }

    @Test
    void weightedSumIsConsistentWithEvaluations() {
        Rubric rubric = rubric(10);
        Evaluation hw = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, null);
        Evaluation exam = Evaluation.create(rubric, student, new CriterionId("EXAM"), new Score(7), null, null);

        StudentTotal total = new TotalCalculator(new WeightedSumStrategy())
                .calculate(student, List.of(hw, exam), rubric);

        assertEquals(7.4, total.value(), 1e-9);           // 8*0.4 + 7*0.6
        assertEquals(2, total.gradedCriteria());
        assertEquals(2, total.totalCriteria());
        assertTrue(total.isComplete());
        assertEquals(3.2, total.weightedParts().get(new CriterionId("HW")), 1e-9);
        assertEquals(4.2, total.weightedParts().get(new CriterionId("EXAM")), 1e-9);
    }

    @Test
    void incompleteRubricIsReportedHonestly() {
        Rubric rubric = rubric(10);
        Evaluation hw = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, null);

        StudentTotal total = new TotalCalculator(new WeightedSumStrategy())
                .calculate(student, List.of(hw), rubric);

        assertEquals(3.2, total.value(), 1e-9);
        assertEquals(1, total.gradedCriteria());
        assertEquals(2, total.totalCriteria());
        assertFalse(total.isComplete());
    }

    @Test
    void emptyEvaluationsGiveZeroTotal() {
        StudentTotal total = new TotalCalculator(new WeightedSumStrategy())
                .calculate(student, List.of(), rubric(10));

        assertEquals(0.0, total.value(), 1e-9);
        assertEquals(0, total.gradedCriteria());
        assertTrue(total.weightedParts().isEmpty());
    }

    @Test
    void strategySwapChangesRuleWithoutTouchingCalculator() {
        Rubric rubric = rubric(20);
        Evaluation hw = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, null);
        Evaluation exam = Evaluation.create(rubric, student, new CriterionId("EXAM"), new Score(14), null, null);
        List<Evaluation> evaluations = List.of(hw, exam);

        double weighted = new TotalCalculator(new WeightedSumStrategy())
                .calculate(student, evaluations, rubric).value();
        double normalized = new TotalCalculator(new NormalizedToTenStrategy())
                .calculate(student, evaluations, rubric).value();

        assertEquals(11.6, weighted, 1e-9);              // 8*0.4 + 14*0.6
        assertEquals(7.25, normalized, 1e-9);            // 10 * 11.6 / (10*0.4 + 20*0.6)
    }

    @Test
    void calculatorDelegatesRuleToInjectedStrategy() {
        Rubric rubric = rubric(10);
        Evaluation hw = Evaluation.create(rubric, student, new CriterionId("HW"), new Score(8), null, null);

        // Стаб правила итога: проверяем, что калькулятор именно ДЕЛЕГИРУЕТ, а не считает сам
        TotalScoreStrategy stub = new TotalScoreStrategy() {
            @Override
            public String name() {
                return "stub";
            }

            @Override
            public double calculate(List<GradedCriterion> graded) {
                return 42.0;
            }

            @Override
            public double part(GradedCriterion graded) {
                return 1.0;
            }
        };

        StudentTotal total = new TotalCalculator(stub).calculate(student, List.of(hw), rubric);

        assertEquals(42.0, total.value(), 1e-9);
        assertEquals("stub", total.strategyName());
        assertEquals(1.0, total.weightedParts().get(new CriterionId("HW")), 1e-9);
    }

    @Test
    void foreignStudentEvaluationIsRejected() {
        Rubric rubric = rubric(10);
        StudentId other = new StudentId("231002");
        Evaluation foreign = Evaluation.create(rubric, other, new CriterionId("HW"), new Score(8), null, null);

        DomainException ex = assertThrows(DomainException.class,
                () -> new TotalCalculator(new WeightedSumStrategy())
                        .calculate(student, List.of(foreign), rubric));
        assertTrue(ex.getMessage().contains("чужого студента"));
    }

    @Test
    void evaluationWithCriterionMissingFromRubricIsRejected() {
        Rubric full = rubric(10);
        Evaluation exam = Evaluation.create(full, student, new CriterionId("EXAM"), new Score(7), null, null);

        Rubric reduced = new Rubric();
        reduced.addCriterion(new CriterionId("HW"), "Домашние задания", 10, new Weight(0.4));

        assertThrows(DomainException.class,
                () -> new TotalCalculator(new WeightedSumStrategy())
                        .calculate(student, List.of(exam), reduced));
    }
}
