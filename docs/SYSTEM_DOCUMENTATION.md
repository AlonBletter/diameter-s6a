# Diameter S6a CSV Processor - System Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Class Diagram](#class-diagram)
4. [Main Flow Diagram](#main-flow-diagram)
5. [Sub-Process Flows](#sub-process-flows)
6. [Component Details](#component-details)
7. [Test Coverage](#test-coverage)

---

## System Overview

The Diameter S6a CSV Processor is a Java application that processes Diameter S6a protocol messages from CSV files. It implements:

- **Message Parsing**: Converts CSV rows into typed Diameter message objects
- **Validation**: Enforces mandatory AVP (Attribute-Value Pair) rules per 3GPP TS 29.272
- **Transaction Management**: Tracks request/answer pairs by Session-Id
- **Reporting**: Generates processing summaries with statistics

### Supported Message Types

| Message | Type | Description |
|---------|------|-------------|
| AIR | Request | Authentication-Information-Request |
| AIA | Answer | Authentication-Information-Answer |
| ULR | Request | Update-Location-Request |
| ULA | Answer | Update-Location-Answer |

---

## Architecture

The system follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                        │
│                         (AppManager)                            │
├─────────────────────────────────────────────────────────────────┤
│    CSV Layer    │   Domain Layer   │   Validation Layer         │
│   (CsvParser)   │ (MessageFactory) │  (MessageValidator)        │
├─────────────────────────────────────────────────────────────────┤
│                     Transaction Layer                           │
│                   (TransactionManager)                          │
├─────────────────────────────────────────────────────────────────┤
│                      Reporting Layer                            │
│                    (SummaryReporter)                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Class Diagram

```mermaid
classDiagram
    direction TB
    
    %% Application Layer
    class AppManager {
        -CsvParser csvParser
        -MessageFactory messageFactory
        -MessageValidator validator
        -TransactionManager transactionManager
        -SummaryReporter summaryReporter
        +run(String[] args) void
        -handleMessagesToTransactions(List~CsvRow~) void
        -processSingleRow(CsvRow) ProcessingResult
        -getCsvRows(String[]) List~CsvRow~
    }

    %% CSV Layer
    class CsvParser {
        <<interface>>
        +parse(List~String~) List~CsvRow~
    }

    class CsvParserImpl {
        -String DELIMITER
        -Map~CsvColumn, Integer~ headerMap
        +parse(List~String~) List~CsvRow~
        -parseLine(String, int) CsvRow
        -validateHeader(String) void
        -validateLine(String[], int) void
    }

    class CsvRow {
        -MessageType messageType
        -boolean isRequest
        -String sessionId
        -String originHost
        -String originRealm
        -String userName
        -String visitedPlmnId
        -String resultCode
        +getMessageType() MessageType
        +getIsRequest() boolean
        +getSessionId() String
        +getOriginHost() String
        +getOriginRealm() String
        +getUserName() String
        +getVisitedPlmnId() String
        +getResultCode() String
    }

    class CsvColumn {
        <<enumeration>>
        MESSAGE_TYPE
        IS_REQUEST
        SESSION_ID
        ORIGIN_HOST
        ORIGIN_REALM
        USER_NAME
        VISITED_PLMN_ID
        RESULT_CODE
    }

    %% Domain Layer - Message Hierarchy
    class DiameterMessage {
        <<abstract>>
        #MessageType messageType
        #boolean isRequest
        #String sessionId
        #String originHost
        #String originRealm
        #String userName
        +validate(ValidationResult) void*
        #require(String, String, ValidationResult) void
        +getMessageType() MessageType
        +getIsRequest() boolean
        +getSessionId() String
    }

    class DiameterRequest {
        <<abstract>>
        +validate(ValidationResult) void
    }

    class DiameterAnswer {
        <<abstract>>
        -String resultCode
        +validate(ValidationResult) void
    }

    class AIR {
        +AIR(String, String, String, String)
    }

    class ULR {
        -String visitedPlmnId
        +ULR(String, String, String, String, String)
        +validate(ValidationResult) void
    }

    class AIA {
        +AIA(String, String, String, String, String)
    }

    class ULA {
        +ULA(String, String, String, String, String)
    }

    class MessageType {
        <<enumeration>>
        AIR
        AIA
        ULR
        ULA
    }

    %% Factory
    class MessageFactory {
        <<interface>>
        +createDiameterMessage(CsvRow) DiameterMessage
    }

    class MessageFactoryImpl {
        -Map~MessageType, MessageDefinition~ messageDefinitions
        +createDiameterMessage(CsvRow) DiameterMessage
    }

    %% Validation Layer
    class MessageValidator {
        <<interface>>
        +validate(DiameterMessage) ValidationResult
    }

    class MessageValidatorImpl {
        +validate(DiameterMessage) ValidationResult
    }

    class ValidationResult {
        -List~String~ errors
        +addError(String) void
        +isValid() boolean
        +getErrors() List~String~
    }

    %% Transaction Layer
    class TransactionManager {
        <<interface>>
        +processDiameterMessage(DiameterMessage) void
        +getTransactionResult() TransactionResult
    }

    class TransactionManagerImpl {
        -TransactionManagerImpl instance$
        -int numberOfCompleteTransactions
        -int numberOfIncompleteTransactions
        -Map~String, Transaction~ transactionsBySessionId
        -Map~MessageType, MessageType~ answerByRequest$
        +getInstance()$ TransactionManagerImpl
        +processDiameterMessage(DiameterMessage) void
        +getTransactionResult() TransactionResult
        -handleRequestMessage(DiameterMessage) void
        -handleAnswerMessage(DiameterMessage) void
    }

    class Transaction {
        -DiameterMessage request
        -DiameterMessage answer
        +getRequest() DiameterMessage
        +getAnswer() DiameterMessage
        +setAnswer(DiameterMessage) void
    }

    class TransactionResult {
        -int numberOfCompleteTransactions
        -int numberOfIncompleteTransactions
        +getNumberOfCompleteTransactions() int
        +getNumberOfIncompleteTransactions() int
    }

    %% Reporter Layer
    class SummaryReporter {
        <<interface>>
        +report(List~ProcessingResult~, TransactionResult) void
    }

    class SummaryReporterImpl {
        +report(List~ProcessingResult~, TransactionResult) void
    }

    class ProcessingResult {
        -boolean success
        -boolean valid
        -String errorMessage
        +success()$ ProcessingResult
        +validationFailure()$ ProcessingResult
        +error(String)$ ProcessingResult
        +isSuccess() boolean
        +isValid() boolean
        +getErrorMessage() String
    }

    %% Exception Hierarchy
    class TransactionException {
        <<exception>>
    }

    class DuplicateTransactionException {
        <<exception>>
    }

    class UnexpectedTransactionAnswerException {
        <<exception>>
    }

    class DiameterMessageValidationException {
        <<exception>>
    }

    class CsvValidationException {
        <<exception>>
    }

    %% Relationships
    CsvParser <|.. CsvParserImpl
    CsvParserImpl --> CsvRow : creates
    CsvParserImpl --> CsvColumn : uses

    DiameterMessage <|-- DiameterRequest
    DiameterMessage <|-- DiameterAnswer
    DiameterRequest <|-- AIR
    DiameterRequest <|-- ULR
    DiameterAnswer <|-- AIA
    DiameterAnswer <|-- ULA
    DiameterMessage --> MessageType : has

    MessageFactory <|.. MessageFactoryImpl
    MessageFactoryImpl --> DiameterMessage : creates
    MessageFactoryImpl --> CsvRow : uses

    MessageValidator <|.. MessageValidatorImpl
    MessageValidatorImpl --> ValidationResult : creates
    MessageValidatorImpl --> DiameterMessage : validates

    TransactionManager <|.. TransactionManagerImpl
    TransactionManagerImpl --> Transaction : manages
    TransactionManagerImpl --> TransactionResult : creates
    Transaction --> DiameterMessage : contains

    SummaryReporter <|.. SummaryReporterImpl
    SummaryReporterImpl --> ProcessingResult : uses
    SummaryReporterImpl --> TransactionResult : uses

    TransactionException <|-- DuplicateTransactionException
    TransactionException <|-- UnexpectedTransactionAnswerException

    AppManager --> CsvParser : uses
    AppManager --> MessageFactory : uses
    AppManager --> MessageValidator : uses
    AppManager --> TransactionManager : uses
    AppManager --> SummaryReporter : uses
    AppManager --> ProcessingResult : creates
```

---

## Main Flow Diagram

```mermaid
flowchart TB
    subgraph Input
        A[CSV File] --> B[FileReader]
    end

    subgraph Parsing
        B --> C[CsvParser]
        C --> D{Valid Header?}
        D -->|No| E[Throw CsvValidationException]
        D -->|Yes| F[Parse Data Lines]
        F --> G[List of CsvRow]
    end

    subgraph Processing["Message Processing Loop"]
        G --> H[For Each CsvRow]
        H --> I[MessageFactory]
        I --> J{Type Match?}
        J -->|No| K[DiameterMessageValidationException]
        J -->|Yes| L[Create DiameterMessage]
        L --> M[MessageValidator]
        M --> N{Valid AVPs?}
        N -->|No| O[ValidationFailure Result]
        N -->|Yes| P[TransactionManager]
        P --> Q{Is Request?}
        Q -->|Yes| R[Open Transaction]
        Q -->|No| S{Has Matching Request?}
        S -->|Yes| T[Complete Transaction]
        S -->|No| U[TransactionException]
        
        K --> V[Error Result]
        O --> W[Collect Results]
        R --> W
        T --> W
        U --> V
        V --> W
    end

    subgraph Output
        W --> X[SummaryReporter]
        X --> Y[Print Summary]
        Y --> Z["Total/Valid/Invalid Messages<br>Completed/Open Transactions"]
    end

    style A fill:#e1f5fe
    style Z fill:#c8e6c9
    style E fill:#ffcdd2
    style K fill:#ffcdd2
    style U fill:#ffcdd2
```

---

## Sub-Process Flows

### CSV Parsing Flow

```mermaid
flowchart TD
    A[Raw CSV Lines] --> B{Lines Empty?}
    B -->|Yes| C[Throw CsvValidationException]
    B -->|No| D[Extract Header Line]
    D --> E[Validate Header]
    E --> F{All Required Columns?}
    F -->|No| G[Throw CsvValidationException]
    F -->|Yes| H[Build Column Index Map]
    H --> I[Process Data Lines]
    
    subgraph LineProcessing["For Each Data Line"]
        I --> J[Split by Delimiter]
        J --> K{Correct Column Count?}
        K -->|No| L[Skip Line + Log Warning]
        K -->|Yes| M{Valid MessageType?}
        M -->|No| L
        M -->|Yes| N{Valid is_request?}
        N -->|No| L
        N -->|Yes| O[Create CsvRow]
        O --> P[Add to Result List]
    end
    
    L --> Q[Continue Loop]
    P --> Q
    Q --> R{More Lines?}
    R -->|Yes| I
    R -->|No| S[Return CsvRow List]

    style C fill:#ffcdd2
    style G fill:#ffcdd2
    style L fill:#fff3e0
    style S fill:#c8e6c9
```

### Message Validation Flow

```mermaid
flowchart TD
    A[DiameterMessage] --> B{Is Request?}
    
    B -->|Yes| C[Validate Request AVPs]
    C --> D{Session-Id Present?}
    D -->|No| E[Add Error: Session-Id mandatory]
    D -->|Yes| F{Origin-Host Present?}
    F -->|No| G[Add Error: Origin-Host mandatory]
    F -->|Yes| H{Origin-Realm Present?}
    H -->|No| I[Add Error: Origin-Realm mandatory]
    H -->|Yes| J{User-Name Present?}
    J -->|No| K[Add Error: User-Name mandatory]
    J -->|Yes| L{Is ULR?}
    L -->|Yes| M{Visited-PLMN-Id Present?}
    M -->|No| N[Add Error: Visited-PLMN-Id mandatory]
    M -->|Yes| O[Validation Complete]
    L -->|No| O
    
    B -->|No| P[Validate Answer AVPs]
    P --> Q{Result-Code Present?}
    Q -->|No| R[Add Error: Result-Code mandatory]
    Q -->|Yes| O
    
    E --> F
    G --> H
    I --> J
    K --> L
    N --> O
    R --> O
    
    O --> S{Any Errors?}
    S -->|Yes| T[Return Invalid Result]
    S -->|No| U[Return Valid Result]

    style T fill:#ffcdd2
    style U fill:#c8e6c9
```

### Transaction Management Flow

```mermaid
flowchart TD
    A[DiameterMessage] --> B{Session-Id Null?}
    B -->|Yes| C[Throw IllegalArgumentException]
    B -->|No| D{Is Request?}
    
    D -->|Yes| E[Handle Request]
    E --> F{Session-Id Exists in Map?}
    F -->|Yes| G[Throw DuplicateTransactionException]
    F -->|No| H[Create New Transaction]
    H --> I[Store in Map by Session-Id]
    I --> J[Increment Incomplete Count]
    
    D -->|No| K[Handle Answer]
    K --> L{Find Transaction by Session-Id}
    L -->|Not Found| M[Throw UnexpectedTransactionAnswerException]
    L -->|Found| N{Message Type Match?}
    N -->|No| O[Ignore - Type Mismatch]
    N -->|Yes| P[Set Answer on Transaction]
    P --> Q[Increment Complete Count]
    Q --> R[Decrement Incomplete Count]
    
    J --> S[Done]
    O --> S
    R --> S

    style C fill:#ffcdd2
    style G fill:#ffcdd2
    style M fill:#ffcdd2
    style S fill:#c8e6c9
```

### Transaction Type Matching

```mermaid
flowchart LR
    subgraph Requests
        AIR[AIR Request]
        ULR[ULR Request]
    end
    
    subgraph Answers
        AIA[AIA Answer]
        ULA[ULA Answer]
    end
    
    AIR -->|Matches| AIA
    ULR -->|Matches| ULA
    
    AIR -.->|Does NOT Match| ULA
    ULR -.->|Does NOT Match| AIA

    style AIR fill:#bbdefb
    style ULR fill:#bbdefb
    style AIA fill:#c8e6c9
    style ULA fill:#c8e6c9
```

---

## Component Details

### CsvParser
**Package:** `diameter.csv.parser`

**Responsibility:** Parse CSV content into CsvRow objects

**Key Features:**
- Dynamic header mapping (columns can be in any order)
- Strict header validation
- Graceful handling of malformed data lines
- Whitespace trimming and empty-to-null conversion

### MessageFactory
**Package:** `diameter.domain`

**Responsibility:** Create typed DiameterMessage objects from CsvRow

**Key Features:**
- Type-safe message creation
- Request/Answer type mismatch detection
- Extensible via MessageDefinition map

### MessageValidator
**Package:** `diameter.validator`

**Responsibility:** Validate mandatory AVPs per message type

**Validation Rules:**

| Message Type | Mandatory AVPs |
|-------------|----------------|
| AIR | Session-Id, Origin-Host, Origin-Realm, User-Name |
| ULR | Session-Id, Origin-Host, Origin-Realm, User-Name, Visited-PLMN-Id |
| AIA | Result-Code |
| ULA | Result-Code |

### TransactionManager
**Package:** `diameter.transaction`

**Responsibility:** Track Diameter transaction lifecycle

**Key Features:**
- Singleton pattern for centralized state
- Session-Id based transaction matching
- Type-pair validation (AIR↔AIA, ULR↔ULA)
- Duplicate request detection

### SummaryReporter
**Package:** `diameter.reporter`

**Responsibility:** Generate and output processing summary

**Output Metrics:**
- Total messages processed
- Valid messages count
- Invalid messages count
- Completed transactions
- Incomplete (open) transactions

---

## Test Coverage

### Test Classes

| Test Class | Package | Coverage |
|------------|---------|----------|
| `CsvParserImplTest` | `diameter.csv.parser` | Header validation, line parsing, edge cases |
| `MessageFactoryImplTest` | `diameter.domain` | Message creation, type mismatch, null handling |
| `DiameterMessageTest` | `diameter.domain.message` | Message hierarchy, validation per type |
| `MessageValidatorImplTest` | `diameter.validator` | AVP validation rules, error collection |
| `TransactionManagerImplTest` | `diameter.transaction` | Transaction lifecycle, matching, exceptions |
| `SummaryReporterImplTest` | `diameter.reporter` | Output formatting, statistics |
| `EndToEndIntegrationTest` | `diameter.integration` | Full pipeline scenarios |
| `CsvDataDrivenTest` | `diameter.integration` | File-based test scenarios |

### Test Data Files

| File | Purpose |
|------|---------|
| `valid_complete_transactions.csv` | Happy path - all transactions complete |
| `open_transactions.csv` | Requests without matching answers |
| `invalid_messages.csv` | Messages with missing mandatory AVPs |
| `spec_example.csv` | Example from specification document |
| `out_of_order_answers.csv` | Answers arriving in different order than requests |
| `type_mismatch.csv` | Request/Answer type mismatches |

### Test Categories

```mermaid
pie title Test Distribution
    "Unit Tests" : 45
    "Integration Tests" : 25
    "Edge Case Tests" : 20
    "Data-Driven Tests" : 10
```

---

## Appendix: Package Structure

```
diameter/
├── app/
│   └── AppManager.java
├── csv/
│   ├── CsvColumn.java
│   ├── model/
│   │   └── CsvRow.java
│   └── parser/
│       ├── CsvParser.java
│       └── CsvParserImpl.java
├── domain/
│   ├── MessageFactory.java
│   ├── MessageFactoryImpl.java
│   ├── MessageType.java
│   └── message/
│       ├── DiameterMessage.java
│       ├── DiameterRequest.java
│       ├── DiameterAnswer.java
│       ├── AIR.java
│       ├── AIA.java
│       ├── ULR.java
│       └── ULA.java
├── exception/
│   ├── csv/
│   │   └── CsvValidationException.java
│   ├── transaction/
│   │   ├── TransactionException.java
│   │   ├── DuplicateTransactionException.java
│   │   └── UnexpectedTransactionAnswerException.java
│   └── validation/
│       └── DiameterMessageValidationException.java
├── io/
│   └── FileReader.java
├── reporter/
│   ├── ProcessingResult.java
│   ├── SummaryReporter.java
│   └── SummaryReporterImpl.java
├── transaction/
│   ├── Transaction.java
│   ├── TransactionManager.java
│   ├── TransactionManagerImpl.java
│   └── TransactionResult.java
└── validator/
    ├── MessageValidator.java
    ├── MessageValidatorImpl.java
    └── ValidationResult.java
```

