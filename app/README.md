# Sistema de Gerenciamento de Sprints

Aplicação JavaFX para gerenciamento de Sprints com arquitetura MVC, utilizando SQLite como banco de dados.

## 🚀 Tecnologias

- **Java 17**
- **JavaFX 17.0.2**
- **Maven** - Gerenciamento de dependências
- **Lombok 1.18.30** - Redução de boilerplate
- **SQLite 3.44.1.0** - Banco de dados embarcado

## 📁 Estrutura do Projeto

```
src/main/java/br/tec/jessebezerra/app/
├── config/
│   └── DatabaseConfig.java          # Configuração do banco de dados
├── controller/
│   └── SprintController.java        # Controller JavaFX
├── dto/
│   └── SprintDTO.java               # Data Transfer Object
├── entity/
│   └── Sprint.java                  # Entidade de domínio
├── repository/
│   └── SprintRepository.java        # Camada de persistência
├── service/
│   └── SprintService.java           # Lógica de negócio
└── HelloApplication.java            # Classe principal

src/main/resources/br/tec/jessebezerra/app/
├── sprint-view.fxml                 # Interface FXML
└── styles/
    └── sprint-styles.css            # Estilos CSS separados
```

## 🏗️ Arquitetura

O projeto segue o padrão **MVC (Model-View-Controller)**:

- **Model**: Entidades (`Sprint`) e DTOs (`SprintDTO`)
- **View**: Arquivos FXML (`sprint-view.fxml`) e CSS (`sprint-styles.css`)
- **Controller**: Controllers JavaFX (`SprintController`)
- **Repository**: Acesso a dados (`SprintRepository`)
- **Service**: Lógica de negócio (`SprintService`)
- **Config**: Configurações (`DatabaseConfig`)

## 📊 Modelo de Dados

### Sprint
- **id**: Long (auto-incremento)
- **nome**: String (nome da sprint)
- **dataInicio**: LocalDate (data de início)
- **duracaoSemanas**: Integer (duração em semanas)

## 🎨 Interface

A interface foi desenvolvida com:
- **Layout responsivo** usando GridPane e VBox
- **Suporte a fullscreen** e maximização
- **CSS moderno** com paleta de cores profissional
- **Componentes estilizados**: botões, tabelas, formulários
- **Feedback visual**: hover effects, sombras, transições

### Funcionalidades da Interface

- ✅ Criar nova sprint
- ✅ Editar sprint existente
- ✅ Excluir sprint (com confirmação)
- ✅ Listar todas as sprints
- ✅ Validação de campos
- ✅ Mensagens de sucesso/erro

## 🔧 Como Executar

### Pré-requisitos
- Java 17 ou superior
- Maven 3.6+

### Executar a aplicação

```bash
mvn clean javafx:run
```

### Compilar o projeto

```bash
mvn clean compile
```

### Gerar executável

```bash
mvn clean package
```

## 💾 Banco de Dados

O banco de dados SQLite é criado automaticamente no primeiro uso:
- Arquivo: `agenda.db` (na raiz do projeto)
- Tabela: `sprint` (criada automaticamente)

### Schema

```sql
CREATE TABLE sprint (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    data_inicio TEXT NOT NULL,
    duracao_semanas INTEGER NOT NULL
)
```

## 🎯 Funcionalidades CRUD

### Create (Criar)
- Preencha os campos: Nome, Data de Início, Duração
- Clique em "Salvar"

### Read (Ler)
- Todas as sprints são listadas automaticamente na tabela
- Ordenadas por data de início (mais recentes primeiro)

### Update (Atualizar)
- Selecione uma sprint na tabela
- Clique em "Editar"
- Modifique os campos desejados
- Clique em "Atualizar"

### Delete (Excluir)
- Selecione uma sprint na tabela
- Clique em "Excluir"
- Confirme a exclusão

## 🎨 Personalização CSS

Os estilos estão separados em `src/main/resources/br/tec/jessebezerra/app/styles/sprint-styles.css`:

- **Cores principais**: #2c3e50, #3498db, #e74c3c, #f39c12
- **Fonte**: Segoe UI, Helvetica Neue, Arial
- **Efeitos**: Sombras, hover states, transições suaves

## 📝 Convenções de Código

- **Lombok**: Usado para getters, setters, construtores
- **Sem Records**: Utilizamos classes tradicionais com Lombok
- **Nomenclatura**: CamelCase para classes, camelCase para métodos
- **Separação de responsabilidades**: Cada camada tem sua função específica

## 🔐 Boas Práticas

- ✅ Separação de camadas (MVC)
- ✅ DTOs para transferência de dados
- ✅ Repository pattern para acesso a dados
- ✅ Service layer para lógica de negócio
- ✅ Validação de entrada
- ✅ Tratamento de exceções
- ✅ Fechamento de recursos (Connection)
- ✅ CSS separado do código Java

## 📦 Dependências Principais

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.2</version>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
</dependency>

<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

## 🐛 Troubleshooting

### Lombok não funciona
- Certifique-se de que o plugin Lombok está instalado na IDE
- Execute `mvn clean compile` para recompilar

### Erro ao carregar FXML
- Verifique se o caminho do FXML está correto
- Confirme que o controller está especificado no FXML

### Banco de dados não cria
- Verifique permissões de escrita na pasta do projeto
- Confirme que o SQLite JDBC está no classpath

## 📄 Licença

Projeto desenvolvido para fins educacionais.

## 👨‍💻 Autor

Jesse Bezerra - Sistema de Gerenciamento de Sprints v1.0
