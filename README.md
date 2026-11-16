# ПОС — Преподавательская Оценка Студентов

Консольный модуль оценивания студентов: кто учится на курсе, по каким критериям оценивают,
какие оценки выставлены и какой получается честный итоговый балл по весам.

ДЗ №2 по курсу «Конструирование программного обеспечения».
Фокус задания: **DDD**, **слоёная архитектура (Ports & Adapters)**, **паттерны GoF по делу**, **DI-контейнер**.

---

## 1. Быстрый старт

### Требования

- JDK 21 (код совместим с 17+);
- Gradle не нужен системный — используется Gradle Wrapper;
- зависимости: Google Guice 7.0.0, JUnit 5.11.4.

### Команды

```bash
# тесты (71 тест)
./gradlew test

# сквозной сценарий из ФТ-4 без интерактива
./gradlew run --args="--demo"

# тот же сценарий, но с другим правилом итога (демонстрация Strategy)
./gradlew run --args="--demo --normalized"

# интерактивное меню
./gradlew run
```

Windows: `gradlew.bat` вместо `./gradlew`.
В IntelliJ IDEA: запустить `pos.Main` (аргументы `--demo`, `--normalized` задаются в Run Configuration).

Аргументы запуска:

| Аргумент | Эффект |
|---|---|
| `--demo` | прогнать `DemoScenario` и завершиться (без чтения stdin) |
| `--normalized` | правило итога «нормализация к 10» вместо взвешенной суммы |

Результат выгрузки: `exports/totals.csv` (создаётся автоматически, в git не коммитится).
HTML-отчёт тестов: `build/reports/tests/test/index.html`.

---

## 2. Функциональные требования → где реализованы

| ФТ | Реализация |
|---|---|
| 1. Студенты и рубрикатор: добавить, посмотреть списки | `AddStudentUseCase`, `AddCriterionUseCase`, `CatalogQueries`; меню 1–4 |
| 2. Выставить / посмотреть / исправить / отменить оценку | `GradeStudentUseCase` (upsert), `CatalogQueries`, `CancelEvaluationUseCase`; меню 5–8 |
| 3. Инварианты и итог | `Score`, `Rubric`, `Evaluation.create/rescore`, `TotalCalculator`; меню 9–10 |
| 4. Сквозной сценарий с экспортом | `DemoScenario`, `ExportResultsUseCase` → порт `ResultExporter` → `CsvResultExporter`; меню 11–12 |

---

## 3. Архитектура: Ports & Adapters (Hexagonal)

Выбрана **Ports & Adapters** в облегчённом учебном варианте. Внутри домена порты хранения
объявлены в самом домене (Onion-стиль): домен формулирует, что ему нужно от хранилища.

### Слои и папки

```text
src/main/java/pos/
├── domain/          ← ЦЕНТР: entities, VO, инварианты, domain service, порты репозиториев
├── application/     ← СЦЕНАРИИ: use cases, фасад, исходящий порт выгрузки
├── infrastructure/  ← АДАПТЕРЫ: память, CSV, консоль, Guice-модуль
└── Main.java        ← COMPOSITION ROOT: собирает граф и запускает UI
```

### Стрелка зависимостей

```text
        infrastructure  ──────►  application  ──────►  domain
              │                                        ▲
              └────────────────────────────────────────┘
                 адаптеры реализуют порты домена
                 (StudentRepository, RubricRepository, EvaluationRepository)
                 и порт application (ResultExporter)
```

Правила, которые соблюдены в коде:

- `pos.domain.**` импортирует только `java.*` и сам домен. Никаких внешних зависимостей.
- `pos.application.**` импортирует только `pos.domain.*` и `java.*`. Про CSV и консоль не знает.
- `pos.infrastructure.**` знает всех: реализует порты, работает с файлами и консолью, содержит Guice-модуль.
- `Main` — единственное место, где создаётся `Injector`.

Проверить это можно механически (все три команды должны вернуть пустоту):

```bash
grep -R "import pos.infrastructure" src/main/java/pos/domain src/main/java/pos/application
grep -R "import com.google.inject"  src/main/java/pos/domain src/main/java/pos/application
grep -R "import java.io\|import java.nio" src/main/java/pos/domain
```

### Порты и адаптеры

| Порт | Где объявлен | Адаптер |
|---|---|---|
| `StudentRepository` | `domain/repository` | `InMemoryStudentRepository` |
| `RubricRepository` | `domain/repository` | `InMemoryRubricRepository` |
| `EvaluationRepository` | `domain/repository` | `InMemoryEvaluationRepository` |
| `ResultExporter` | `application/port/out` | `CsvResultExporter` |

Почему порты хранения в домене, а порт выгрузки в application:
хранение — потребность доменной модели (репозитории агрегатов), а выгрузка итогов — прикладной сценарий,
и формат строки выгрузки (`TotalRow`) — прикладная концепция, а не доменная.

### Composition root

`Main` выбирает стратегию итога и путь выгрузки, передаёт их в `AppModule`, поднимает Guice
и достаёт `ConsoleMenu` или `DemoScenario`. `AppModule` — единственный класс, где перечислены
конкретные реализации (`@Provides @Singleton`). В домене и application **нет ни одной Guice-аннотации**.

---

## 4. Карта DDD

### 4.1 Ubiquitous Language

Слова заказчика совпадают с именами в коде.

| Термин предметной области | Тип в коде |
|---|---|
| студент | `Student` (Entity) |
| критерий | `Criterion` (Entity внутри агрегата) |
| рубрикатор | `Rubric` (Aggregate Root) |
| оценка (факт оценивания) | `Evaluation` (Entity) |
| балл | `Score` (Value Object) |
| вклад критерия в итог / вес | `Weight` (Value Object) |
| итог | `StudentTotal` (VO — результат расчёта) |
| максимум критерия | `Criterion.maxScore()` |
| выставить оценку | `GradeStudentUseCase` |
| посчитать итог | `TotalCalculator` (Domain Service) |
| выгрузка итогов | `ExportResultsUseCase` + порт `ResultExporter` |

### 4.2 Entity и Value Object — осознанное разделение

**Entity — есть идентичность, состояние может меняться:**

- `Student`: идентичность по `StudentId`. Два студента с одинаковым ФИО — разные люди;
  `equals/hashCode` только по id. ФИО и группу можно исправить (`rename`, `changeGroup`),
  identity при этом не меняется.
  Тест: `StudentTest#identityIsBasedOnIdNotOnName`, `#renameKeepsIdentity`.
- `Evaluation`: идентичность по `EvaluationId`. Балл можно исправить (`rescore`),
  но это остаётся тот же факт оценивания, а не новый.
  Тест: `GradeStudentUseCaseTest#repeatedGradeRescoresSameEvaluationInsteadOfDuplicating`.
- `Criterion`: идентичность по `CriterionId` внутри агрегата `Rubric`.

**Value Object — равенство по значению, изменяется только заменой целиком:**

- `Score`, `Weight` — числа с инвариантами; нельзя «мутировать 8 в 9», создаётся новый `Score`,
  и `Evaluation.rescore` заменяет ссылку.
- `StudentId`, `CriterionId`, `EvaluationId` — типизированные идентификаторы (records),
  нормализуют ввод (trim, upper-case для кода критерия).
- `StudentTotal` — снимок результата расчёта с детализацией; immutable record.
- `GradedCriterion` — пара «оценка + её критерий», вход правила итога.

Пример разделения для защиты: `Student` — Entity (важно, **кто** именно), `Score` — VO
(важно **сколько**, а не «какой экземпляр восьмёрки»).

### 4.3 Агрегат и его граница

**`Rubric` — корень агрегата**, внутри — `Criterion`.

Граница выражена в коде, а не только на словах:

- конструктор `Criterion` — **package-private**: извне создать критерий нельзя;
- единственный путь создания — `Rubric.addCriterion(...)`, где проверяются дубликаты,
  положительность максимума, непустое название;
- единственный путь доступа — `Rubric.require(id)`, который бросает `DomainException`,
  если критерия нет (это и есть охрана инварианта «оценка только по критерию рубрикатора»);
- `Rubric.criteria()` возвращает неизменяемый снимок — внутреннюю коллекцию не подменить;
- `RubricRepository` грузит/сохраняет агрегат целиком.

Тесты: `RubricTest#duplicateCriterionIsRejected`, `#unknownCriterionCannotBeRequired`,
`#criteriaListIsImmutableSnapshot`.

**Ссылки между агрегатами — по идентификатору, а не по объектной ссылке:**
`Evaluation` хранит `StudentId` и `CriterionId`, а не `Student` и `Criterion`.
Это не даёт агрегатам переплестись и позволяет грузить их независимо.

### 4.4 Инварианты: где охраняются и чем зафиксированы

| Инвариант | Где охраняется | Тест-свидетель |
|---|---|---|
| Балл не выходит за пределы критерия (`0 ≤ score ≤ max`) | `Score` (конструктор + `requireWithinMax`), `Evaluation.create` / `rescore` | `ScoreTest#scoreAboveMaxIsRejected`, `EvaluationFactoryTest#scoreAboveCriterionMaxIsRejected`, `#failedRescoreKeepsPreviousScore`, `GradeStudentUseCaseTest#scoreAboveCriterionMaxIsRejectedAndNothingSaved` |
| Оценка только по критерию из рубрикатора | `Rubric.require`, вызывается из `Evaluation.create` / `rescore` и `TotalCalculator` | `RubricTest#unknownCriterionCannotBeRequired`, `EvaluationFactoryTest#criterionOutsideRubricIsRejected`, `TotalCalculatorTest#evaluationWithCriterionMissingFromRubricIsRejected` |
| Итог согласован с текущим набором оценок | `TotalCalculator`: итог **нигде не хранится**, всегда вычисляется заново | `TotalCalculatorTest#weightedSumIsConsistentWithEvaluations`, `CalculateTotalUseCaseTest#rescoreIsImmediatelyReflectedInTotal`, `#cancelIsImmediatelyReflectedInTotal` |

Дополнительные правила, введённые по смыслу домена:

- вес критерия строго положительный (`Weight`), максимум критерия положительный (`Criterion`);
- код критерия в рубрикаторе уникален (`Rubric.addCriterion`);
- id студента на курсе уникален (`AddStudentUseCase`);
- на пару (студент, критерий) — не более одной оценки (`GradeStudentUseCase`, upsert);
- нельзя считать итог по чужой оценке (`TotalCalculator` проверяет принадлежность).

Все нарушения — `DomainException` с человекочитаемым сообщением; консоль показывает его,
а не падает со стектрейсом.

### 4.5 Repository (порт) — хранение снаружи домена

Интерфейсы `StudentRepository`, `RubricRepository`, `EvaluationRepository` лежат в
`pos.domain.repository` и не содержат ни слова о способе хранения. Реализации в памяти —
в `pos.infrastructure.persistence`. Домен и сценарии работают только с портами,
поэтому подмена хранилища (fake в тестах, другой адаптер в будущем) не требует правок модели.

### 4.6 Domain Service — расчёт итога

`TotalCalculator` — доменный сервис, потому что правило **не принадлежит одной сущности**:
итог зависит от оценок студента, рубрикатора и правила пересчёта.

- ни `Student`, ни `Evaluation`, ни `Rubric` не знают про итог;
- сервис собирает пары `GradedCriterion`, проверяя принадлежность оценок студенту
  и наличие критериев в рубрикаторе;
- само правило пересчёта делегируется `TotalScoreStrategy`;
- результат — immutable `StudentTotal` с детализацией вклада каждого критерия,
  именем правила и счётчиком «оценено N из M».

### 4.7 Почему это не анемичная модель

- валидация живёт **внутри** модели: конструкторы VO, приватный конструктор `Evaluation`
  + фабрика, package-private конструктор `Criterion` + агрегат;
- use cases тонкие **по замыслу**: загрузить агрегат → вызвать домен → сохранить.
  Они не содержат правил вида «если score > max, то…»;
- домен тестируется без БД, файлов, консоли и DI (38 тестов) — признак того,
  что поведение действительно в модели.

---

## 5. Паттерны GoF: по одному из каждой группы

Применены **три** паттерна — по одному из порождающих, структурных и поведенческих.
Каждый закрывает конкретную боль; «для галочки» ничего не добавлено.

### 5.1 Порождающий — Factory Method (единая точка создания с инвариантами)

**Где:** `pos.domain.model.Evaluation` — приватный конструктор + статический фабричный метод
`Evaluation.create(rubric, studentId, criterionId, score, date, comment)`.

**Боль:** оценку нельзя создать в обход правил домена. Если оставить публичный конструктор,
любой сценарий (или тест, или будущий импорт ведомости) сможет собрать «12 из 10»
или оценку по несуществующему критерию, и инварианты окажутся размазаны по сервисам.

**Как закрыта:** объект физически невозможно сконструировать иначе, чем через фабрику,
а фабрика сначала спрашивает рубрикатор (`rubric.require`) и границы балла
(`score.requireWithinMax`). Тот же путь используется при исправлении (`rescore`).
Это ровно пример порождающего паттерна, предложенный в ТЗ.

**Тесты:** `EvaluationFactoryTest` (7 тестов), `GradeStudentUseCaseTest#failedCorrectionKeepsPreviousScore`.

*Терминологическая честность:* реализована статическая фабрика — разновидность фабричного
подхода без наследников. Канонический GoF Factory Method предполагает подклассы-создатели;
здесь вариативность создания не нужна, поэтому ввод иерархии фабрик был бы overengineering.

### 5.2 Структурный — Facade

**Где:** `pos.application.PosFacade`.

**Боль:** без фасада `ConsoleMenu` знала бы про семь сценариев, сама собирала бы `StudentId`,
`CriterionId`, `Score`, `Weight` из строк и оркестровывала вызовы — сценарии «размазались» бы по UI,
а замена консоли на другой интерфейс потребовала бы дублирования логики.

**Как закрыта:** фасад — единственная дверь для UI. Он принимает примитивы
(`grade("231001", "hw", 8, "комментарий")`), мапит их в VO на границе и делегирует use cases.
Консоль не импортирует ни один use case и ни один VO-конструктор.

**Тесты:** `PosFacadeTest#facadeMapsPrimitivesToValueObjects`, `#wholeScenarioWorksThroughSingleFacade`.

### 5.3 Поведенческий — Strategy

**Где:** `pos.domain.service.TotalScoreStrategy` + `WeightedSumStrategy` (по умолчанию)
и `NormalizedToTenStrategy`; используется в `TotalCalculator`.

**Боль:** заказчик меняет правило итога («взвешенная сумма», «привести к шкале 0..10»,
в будущем — «лучшая попытка пересдачи»). Без стратегии формула была бы зашита в калькулятор,
и каждое изменение правила означало бы правку доменного сервиса и регресс в тестах.

**Как закрыта:** правило — отдельный интерфейс с двумя реализациями. Смена правила =
одна привязка в `AppModule.totalScoreStrategy()` или аргумент `--normalized`.
Домен, сценарии и UI не меняются.

Формулы:

- `WeightedSumStrategy`: `итог = Σ (балл × вес)`;
- `NormalizedToTenStrategy`: `итог = 10 × Σ(балл × вес) / Σ(максимум × вес)` по оценённым критериям.

**Тесты:** `TotalCalculatorTest#strategySwapChangesRuleWithoutTouchingCalculator`,
`#calculatorDelegatesRuleToInjectedStrategy` (стаб правила),
`CalculateTotalUseCaseTest#injectedStrategyDefinesTheRule`,
`CompositionRootTest#strategyBindingChangesTotalsWithoutTouchingDomainOrApplication`.

### 5.4 Почему не добавлены другие паттерны

- **Decorator (округление)** — требования к округлению нет; форматирование до двух знаков
  живёт в адаптерах (`ConsoleView`, `CsvResultExporter`), где ему и место.
- **Builder (рубрикатор)** — рубрикатор собирается одним методом `addCriterion` с инвариантами;
  многошаговая сборка не нужна.
- **Template Method (импорт ведомостей)** — импорт не входит в функциональные требования;
  появился бы вместе с реальным импортом CSV/JSON.

---

## 6. DI-контейнер и тесты

### 6.1 DI

Google Guice. Граф собирается на краю: `Main` → `AppModule` (`@Provides @Singleton`).
Репозитории — синглтоны, поэтому все сценарии видят одни и те же данные
(тест `CompositionRootTest#repositoriesAreSingletonsInsideContainer`).

### 6.2 Виды подмен в тестах

| Вид | Пример | Что проверяет |
|---|---|---|
| **Fake** | `FakeStudentRepository`, `FakeRubricRepository`, `FakeEvaluationRepository` | сценарии application тестируются **без инфраструктуры вообще** |
| **Stub** | `RecordingExporterStub` | что именно и сколько раз ушло в порт выгрузки |
| **Stub правила** | анонимная `TotalScoreStrategy` в `TotalCalculatorTest` | калькулятор делегирует правило, а не считает сам |
| **Подмена через DI** | `AppModule` с другой стратегией в `CompositionRootTest` | смена правила на уровне контейнера меняет результат |

### 6.3 Состав тестов: 71 тест

Домен (38):

- `ScoreTest` (6), `WeightTest` (4), `IdentifiersTest` (5), `StudentTest` (3);
- `RubricTest` (6), `EvaluationFactoryTest` (7);
- `TotalCalculatorTest` (7).

Application и граница (33):

- `AddStudentUseCaseTest` (3), `AddCriterionUseCaseTest` (3);
- `GradeStudentUseCaseTest` (7), `CancelEvaluationUseCaseTest` (2);
- `CalculateTotalUseCaseTest` (6), `ExportResultsUseCaseTest` (3), `PosFacadeTest` (2);
- `CompositionRootTest` (3), `CsvResultExporterTest` (2), `DemoScenarioTest` (2).

Покрыты домен, все сценарии, адаптеры хранения и CSV, демо-сценарий и рендер отчётов.
Не покрыт интерактивный `ConsoleMenu` (ввод с клавиатуры) и `Main` — тонкий UI-слой;
его поведение проверяется запуском `--demo` и вручную.

Ключевые поведенческие тесты для защиты:

- исправление оценки — upsert, а не дубль: `GradeStudentUseCaseTest#repeatedGradeRescoresSameEvaluationInsteadOfDuplicating`;
- неудачное исправление не портит данные: `#failedCorrectionKeepsPreviousScore`;
- итог мгновенно реагирует на исправление и отмену: `CalculateTotalUseCaseTest#rescoreIsImmediatelyReflectedInTotal`, `#cancelIsImmediatelyReflectedInTotal`;
- выгрузка всегда отдаёт актуальные данные: `ExportResultsUseCaseTest#exportAlwaysUsesLatestEvaluations`;
- сквозной сценарий работает: `DemoScenarioTest#demoShowsInvariantsTotalsAndExport`.

---

## 7. Демонстрация

### 7.1 Сквозной сценарий (`--demo`, пункт меню 12)

Рубрикатор: `HW` (макс 10, вес 0.4), `PROJECT` (макс 10, вес 0.2), `EXAM` (макс 20, вес 0.4).
Студенты: `231001` Иванов Иван (БПИ231), `231002` Петрова Анна (БПИ232).

1. Заводим студентов и критерии, показываем списки.
2. Выставляем оценки: Иванов HW 8, PROJECT 9, EXAM 15; Петрова HW 6, EXAM 12.
3. Пробуем нарушить правила — три ожидаемых отказа с пометкой `[ИНВАРИАНТ СРАБОТАЛ]`:
   12 баллов из 10; критерий `ORAL` вне рубрикатора; оценка несуществующему студенту.
4. Считаем итоги с детализацией вклада каждого критерия.
5. Исправляем Иванову HW 8 → 9 и отменяем Петровой EXAM, затем выставляем 14 —
   итог пересчитывается сам, флаг «неполно» появляется и исчезает.
6. Выгружаем итоги в CSV, печатаем путь файла.

Контрольные числа (взвешенная сумма):

- Иванов после исправления: `9×0.4 + 9×0.2 + 15×0.4 = 11.40`, оценено 3 из 3;
- Петрова после отмены EXAM: `6×0.4 = 2.40`, 1 из 3 (помечено «неполно»);
- Петрова после повторного EXAM = 14: `2.4 + 5.6 = 8.00`, 2 из 3.

С `--normalized` на тех же данных: Иванов `8.14`, Петрова `6.67` — правило сменилось
без правок домена и сценариев.

Повторный запуск демо безопасен: дубликаты на этапе заводки пропускаются с пометкой `[ПРОПУСК]`
(тест `DemoScenarioTest#repeatedRunIsSafeAndSkipsDuplicates`).

### 7.2 Формат выгрузки

`exports/totals.csv`:

```csv
student_id,full_name,group,total,strategy,graded_criteria,total_criteria,complete
231001,Иванов Иван,БПИ231,11.40,Взвешенная сумма (Σ балл × вес),3,3,yes
231002,Петрова Анна,БПИ232,8.00,Взвешенная сумма (Σ балл × вес),2,3,no
```

Числа форматируются в `Locale.ROOT` (точка как разделитель), поля с запятыми и кавычками
экранируются по правилам CSV, родительские каталоги создаются автоматически.

### 7.3 Интерактивное меню

```text
 1) Добавить студента          2) Список студентов
 3) Добавить критерий          4) Список критериев
 5) Выставить/исправить оценку 6) Оценки студента
 7) Все оценки                 8) Отменить оценку
 9) Итог по студенту          10) Итоги по всем
11) Экспорт итогов (CSV)      12) Демо-сценарий
 0) Выход
```

Ошибки ввода и нарушения домена перехватываются и печатаются как понятные сообщения
(`[ОТКАЗ ДОМЕНА]`, `[ОШИБКА ВВОДА]`, `[ОШИБКА ВЫГРУЗКИ]`) — приложение не падает.
Дробные числа принимаются и с точкой, и с запятой.

---

## 8. Осознанные ограничения и расширяемость

### 8.1 Что ограничено сознательно

1. **Один курс — один рубрикатор.** `RubricRepository.load()` без параметров.
2. **Веса не обязаны суммироваться в 1.** Заказчик говорит про «вклад в итог»,
   нормализация — забота правила итога (`NormalizedToTenStrategy`), а не инвариант веса.
3. **Итог по неполному набору** считается по оценённым критериям и честно помечается
   `complete = false` / «неполно». Альтернатива «считать неоценённое нулём» —
   другое правило, реализуется ещё одной стратегией без правки калькулятора.
4. **Хранение в памяти.** Персистентность между запусками не требуется ФТ;
   CSV — только выгрузка итогов.
5. **Статическая фабрика** вместо иерархии фабрик (см. 5.1).

### 8.2 Что сломается / не сломается при развитии

| Расширение | Влияние на код |
|---|---|
| Новый критерий оценивания | Ничего не меняется: это данные, а не код (`addCriterion`) |
| Новое правило итога («лучшая попытка», округление) | Новый класс `TotalScoreStrategy` + одна привязка в `AppModule`. Домен, сценарии, UI не трогаем |
| Хранение в JSON/БД вместо памяти | Новый адаптер порта + одна привязка в `AppModule` |
| Выгрузка в JSON вместо CSV | Новый адаптер `ResultExporter`; `TotalRow` и сценарий не меняются |
| Другой UI (JavaFX / веб) | Использует `PosFacade`; use cases и домен не меняются |
| Пересдачи | `Evaluation` уже хранит дату. Понадобится снять ограничение «одна оценка на пару»: изменить семантику `GradeStudentUseCase` и `EvaluationRepository.findByStudentAndCriterion`, добавить понятие попытки. Инварианты балла и рубрикатора сохраняются, итог — новая стратегия |
| Апелляции | Новый агрегат `Appeal` со своим состоянием и сценарии; `TotalCalculator` не меняется, если итог считается по принятым оценкам |
| Несколько курсов | Самое дорогое расширение: `RubricRepository.load(CourseId)`, агрегат `Course`, правки `AddCriterionUseCase` / `CalculateTotalUseCase` и адаптера. Локализовано в портах и сценариях, доменная модель оценок не переписывается |

---

## 9. Структура репозитория

```text
KPO_DZ2/
├── .gitignore
├── README.md
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat / gradle/wrapper/…
└── src/
    ├── main/java/pos/
    │   ├── Main.java
    │   ├── domain/
    │   │   ├── exception/DomainException.java
    │   │   ├── model/  StudentId, Student, CriterionId, Score, Weight,
    │   │   │           Criterion, Rubric, EvaluationId, Evaluation, StudentTotal
    │   │   ├── repository/  StudentRepository, RubricRepository, EvaluationRepository
    │   │   └── service/  TotalScoreStrategy, GradedCriterion, TotalCalculator,
    │   │                 strategy/WeightedSumStrategy, strategy/NormalizedToTenStrategy
    │   ├── application/
    │   │   ├── PosFacade.java
    │   │   ├── port/out/  ResultExporter, TotalRow
    │   │   └── usecase/  AddStudent, AddCriterion, CatalogQueries, GradeStudent,
    │   │                 CancelEvaluation, CalculateTotal, ExportResults
    │   └── infrastructure/
    │       ├── di/AppModule.java
    │       ├── persistence/  InMemoryStudentRepository, InMemoryRubricRepository,
    │       │                 InMemoryEvaluationRepository
    │       ├── export/CsvResultExporter.java
    │       └── ui/  ConsoleView, ConsoleMenu, DemoScenario
    └── test/java/pos/
        ├── CompositionRootTest.java
        ├── domain/  ScoreTest, WeightTest, IdentifiersTest, StudentTest,
        │            RubricTest, EvaluationFactoryTest, TotalCalculatorTest
        ├── application/  ApplicationFixture + 7 тестов сценариев и фасада
        ├── infrastructure/  CsvResultExporterTest, DemoScenarioTest
        └── testdoubles/  FakeStudentRepository, FakeRubricRepository,
                          FakeEvaluationRepository, RecordingExporterStub
```

---

## 10. Роль ИИ и что сделано самостоятельно

**Что делал ИИ-ассистент:**

- предложил вариант декомпозиции предметной области на слои и пакеты по тексту ТЗ;
- помог сформулировать контракты портов (`StudentRepository`, `RubricRepository`,
  `EvaluationRepository`, `ResultExporter`) и границу агрегата `Rubric`;
- сгенерировал первые версии доменных классов, сценариев, адаптеров и тестов;
- помог оформить структуру этого README.

**Что делал я:**

- выбрал архитектуру (Ports & Adapters), правило итога по умолчанию и семантику upsert для оценок;
- добавил `junit-platform-launcher` для Gradle 9 и проброс `standardInput` для интерактивного меню;
- запускал и проверял каждый слой: `./gradlew test`, `--demo`, `--demo --normalized`,
  интерактивное меню; сверял контрольные числа итогов вручную;
- проверял соблюдение стрелки зависимостей (поиск импортов в `domain`/`application`);
- настроил git и `.gitignore`, чтобы в репозиторий не попали `.idea/`, `build/` и файлы выгрузки;
- разобрал код и могу объяснить: почему `Criterion` создаётся только через `Rubric`,
  почему итог нигде не хранится, чем fake отличается от stub, как сменить правило итога.