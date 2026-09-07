# StudyApp — Fluxo de Pagamento POS

## 1. Visão Geral

Mini-projeto de fluxo de pagamento (POS/Adquirência) desenvolvido em **Kotlin e Jetpack Compose**, com gerenciamento de estado por ViewModel, corrotinas e injeção de dependências com Hilt.

A tela permite iniciar uma cobrança demonstrativa de **R$ 150,00 no crédito**, acompanhar o processamento e visualizar aprovação ou erro. O modelo contempla crédito, débito e Pix, embora a interface atual ofereça apenas a cobrança fixa no crédito.

O processamento é simulado em `PaymentRepositoryImpl`: valores menores ou iguais a zero produzem falha; valores positivos abaixo de R$ 9.999,00 são aprovados; valores iguais ou superiores a esse limite são recusados. O histórico fica em memória, sem persistência entre processos. Não há integração com hardware POS ou adquirente real, nem tela de histórico.

## 2. Decisões Arquiteturais

### UI Layer + Data Layer: abordagem pragmática da Google

A arquitetura adota a separação de responsabilidades entre **camada de UI** e **camada de dados**:

- **UI Layer:** `PaymentRoute` conecta o ViewModel ao Compose; `PaymentScreen` renderiza estados e emite intenções do usuário; `PaymentViewModel` coordena o pagamento e transforma resultados em estado de tela.
- **Data Layer:** `PaymentRepository` define o contrato de processamento e consulta do histórico. `PaymentRepositoryImpl` concentra validação do valor, simulação do processamento e armazenamento em memória.

A camada de domínio e os **UseCases são opcionais** nessa abordagem. Eles passam a ser úteis quando há lógica complexa ou compartilhada entre vários ViewModels, ou quando é necessário coordenar diferentes repositórios. Neste escopo, um `ProcessPaymentUseCase` que apenas chamasse `repository.processPayment(...)` seria um *pass-through*: acrescentaria uma etapa sem encapsular comportamento adicional. Por isso, o ViewModel depende diretamente da interface do repositório.

**Correspondência com o código:** os modelos e repositórios estão atualmente em um pacote chamado `domain`. Esse nome não representa uma camada adicional de UseCases: inclusive a implementação de dados está ali. A seção 3 explicita a organização proposta em `data`, distinguindo-a dos caminhos existentes.

### Fluxo Unidirecional de Dados (UDF)

```text
Usuário → PaymentScreen → PaymentUiEvent → PaymentViewModel
                                                ↓
                                         PaymentRepository
                                                ↓
PaymentScreen ← PaymentRoute ← StateFlow<PaymentUiState>
```

`PaymentUiEvent` é uma `sealed interface` de intenções recebidas pelo ViewModel: `OnProcessPaymentClicked(amount, type)` inicia a cobrança e `OnResetClicked` retorna ao estado inicial. Não se trata de um canal de efeitos de saída.

O ViewModel mantém um `MutableStateFlow` privado e expõe somente `StateFlow<PaymentUiState>` com `asStateFlow()`. `PaymentUiState` é uma `sealed interface` que delimita os estados possíveis:

| Estado | Responsabilidade na interface |
| --- | --- |
| `Idle` | Exibir a ação de cobrança. |
| `Processing` | Exibir indicador e mensagem de processamento. |
| `Success(transaction)` | Exibir os dados da transação aprovada. |
| `Error(message)` | Exibir o motivo do erro e permitir reiniciar. |

`PaymentRoute` coleta o fluxo com `collectAsStateWithLifecycle()`. `PaymentScreen` recebe apenas `state` e `onEvent`, o que permite renderizá-la e testá-la independentemente do ViewModel. O caminho de pagamento é `Idle → Processing → Success/Error`; o evento de reinício define `Idle`.

O ViewModel diferencia falhas de execução (`Result.failure`) de recusas de negócio (`Result.success` contendo `TransactionStatus.Declined`) e converte ambas em mensagens de erro para a UI.

### Aplicação prática de SOLID

- **SRP — responsabilidade única:** a tela apresenta informações, o ViewModel gerencia o estado, o repositório controla o processamento e histórico, e o módulo Hilt configura dependências. Cada componente tem um motivo de mudança associado à sua função.
- **OCP — aberto para extensão, fechado para modificação:** a interface `PaymentRepository` permite substituir o simulador por outra implementação, preservando o contrato consumido pelo ViewModel. Uma futura integração pode introduzir gateways para diferentes adquirentes, mantendo detalhes de SDK ou rede fora da UI. Esses gateways são uma possibilidade de evolução; ainda não existem no projeto.
- **DIP — inversão de dependência:** `PaymentViewModel` depende de `PaymentRepository`, e não de `PaymentRepositoryImpl`. A implementação recebe um `CoroutineDispatcher` pelo construtor, permitindo substituir `Dispatchers.IO` por um dispatcher controlado nos testes.

### Injeção de Dependências com Hilt

`MainApplication`, anotada com `@HiltAndroidApp`, inicializa o contêiner. `MainActivity` utiliza `@AndroidEntryPoint` e obtém `PaymentViewModel` por `by viewModels()`. O ViewModel usa `@HiltViewModel` e injeção por construtor.

Em `PaymentModule`, `@Module` e `@InstallIn(SingletonComponent::class)` registram as dependências no componente da aplicação:

- `@Binds` associa a interface `PaymentRepository` a `PaymentRepositoryImpl`.
- `@Singleton` mantém uma instância compartilhada do repositório nesse componente, incluindo seu histórico em memória.
- `@Provides` fornece `Dispatchers.IO` como `CoroutineDispatcher`, também com escopo `@Singleton`.

O escopo singleton não torna o histórico persistente: seus dados continuam limitados à vida do processo.

## 3. Estrutura de Pastas

Organização proposta para o pacote `app/src/main/java/com/example/studyapp/feature/payment`, alinhada às responsabilidades de UI + Data:

```text
feature/payment/
├── data/
│   ├── model/
│   │   └── Transaction.kt           # Transaction, PaymentType e TransactionStatus
│   └── repository/
│       ├── PaymentRepository.kt     # Contrato
│       └── PaymentRepositoryImpl.kt # Simulação e histórico em memória
├── presentation/
│   ├── PaymentContract.kt           # PaymentUiState e PaymentUiEvent
│   ├── PaymentScreen.kt             # PaymentRoute e PaymentScreen
│   └── PaymentViewModel.kt          # Eventos e estado observável
└── di/
    └── PaymentModule.kt             # Bindings e dispatcher
```

**Estado atual do repositório:** os arquivos representados em `data/model/` e `data/repository/` estão fisicamente em `domain/model/` e `domain/repository/`. Os diretórios `presentation/` e `di/` já correspondem à árvore. A árvore documenta a nomenclatura proposta; a criação deste README não move arquivos nem altera imports.

## 4. Estratégia de Testes

| Critério | Testes Unitários de JVM | Testes de Componentes/UI |
| --- | --- | --- |
| Localização | `app/src/test/` | `app/src/androidTest/` |
| Ambiente | JVM local, sem dispositivo Android. | Emulador ou dispositivo Android. |
| Ferramentas | JUnit4, MockK, Coroutines-Test e Turbine. | AndroidJUnit4 e Compose Test Rule (`createComposeRule`). |
| Escopo | Regras do repositório e transições do ViewModel. | Renderização de `PaymentScreen` e emissão de eventos por interação. |
| Isolamento | MockK substitui o repositório nos testes do ViewModel; o repositório concreto recebe dispatcher de teste. | Estados são fornecidos diretamente ao composable e eventos são capturados por callback. |
| Assincronismo | `runTest`, `StandardTestDispatcher` e controle do scheduler; Turbine observa emissões do `StateFlow`. | Compose Test Rule sincroniza interações e verificações com a UI. |
| Verificação | Resultados, estados emitidos e chamadas ao contrato com `coVerify`. | Semantics Tree: localização por texto, `assertIsDisplayed()` e `performClick()`. |
| Comando | `./gradlew test` | `./gradlew connectedAndroidTest` |

A **Semantics Tree** expõe propriedades e ações dos componentes Compose para acessibilidade e testes. Os testes localizam elementos por seus textos e exercitam cliques; não comparam pixels nem dependem da hierarquia tradicional de Views.

Cobertura presente no código:

- `PaymentRepositoryImplTest`: aprovação, recusa acima do limite e rejeição do valor zero. Apesar do nome de um teste mencionar negativos, o caso executado usa apenas zero.
- `PaymentViewModelTest`: estado inicial, aprovação, recusa e evento de reinício enquanto já está em `Idle`.
- `PaymentScreenTest`: apresentação de `Idle`, `Processing`, `Success` e `Error`, além do evento emitido pelo botão de cobrança.

Os testes de UI são testes de componente: instanciam `PaymentScreen` isoladamente e não validam uma integração completa entre Activity, Hilt e repositório. Casos como histórico, valor negativo, limite exato e reinício após sucesso/erro são oportunidades de ampliação da cobertura.

## 5. Como Executar

### Pré-requisitos

- Android Studio compatível com a configuração Gradle do projeto.
- Android SDK com a plataforma **API 37**, usada por `compileSdk` e `targetSdk`, e licenças aceitas.
- JVM **25** para o daemon, conforme `gradle/gradle-daemon-jvm.properties`, instalada ou provisionada pelo Gradle. A compatibilidade de código Java configurada no módulo é 11, distinta da versão da JVM do daemon.
- SDK configurado no Android Studio ou por `sdk.dir` em `local.properties`.
- Acesso aos repositórios de dependências durante a primeira sincronização.
- Para executar o aplicativo e os testes instrumentados: emulador iniciado ou dispositivo conectado com depuração USB autorizada, usando **Android 8.0 / API 26 ou superior** (`minSdk`).

Abra a raiz do projeto no Android Studio, sincronize o Gradle, selecione o módulo `app` e execute-o no dispositivo escolhido. Use o Gradle Wrapper incluído no repositório para os comandos abaixo, sempre a partir da raiz.

### Testes unitários

```bash
./gradlew test
```

### Testes de componentes/UI

Com um emulador ou dispositivo disponível:

```bash
./gradlew connectedAndroidTest
```

No Windows PowerShell, os comandos equivalentes são:

```powershell
.\gradlew.bat test
.\gradlew.bat connectedAndroidTest
```

Os relatórios ficam sob `app/build/reports/tests/` para testes unitários e `app/build/reports/androidTests/` para testes instrumentados. A descrição da cobertura acima foi baseada na leitura dos testes; não constitui um registro de execução bem-sucedida.
