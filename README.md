# 🍪 Oreo Insight Factory - Hackathon #1

## 👥 Información del Equipo

**Nombre del Equipo**: [Tu Nombre de Equipo]

**Integrantes**:
1. [Nombre Completo 1] - Código UTEC: [U000000]
2. [Nombre Completo 2] - Código UTEC: [U000000]
3. [Nombre Completo 3] - Código UTEC: [U000000]
4. [Nombre Completo 4] - Código UTEC: [U000000]
5. [Nombre Completo 5] - Código UTEC: [U000000]

---

## 🚀 Descripción del Proyecto

Backend completo para el sistema de análisis de ventas de Oreo con:
- ✅ Autenticación JWT con roles (CENTRAL y BRANCH)
- ✅ CRUD completo de ventas con permisos por sucursal
- ✅ Procesamiento asíncrono de reportes con @Async y eventos
- ✅ Integración con GitHub Models (LLM) para generación de resúmenes
- ✅ Envío automatizado de emails con reportes
- ✅ Testing unitario completo
- ✅ **BONUS**: Reportes premium con HTML, gráficos y PDF adjunto

---

## 🛠️ Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring Security + JWT**
- **Spring Data JPA**
- **H2 Database** (en memoria)
- **Spring Boot Mail**
- **WebFlux** (para cliente HTTP)
- **iText7** (generación de PDFs)
- **JUnit 5 + Mockito** (testing)

---

## ⚙️ Configuración del Proyecto

### 1. Prerrequisitos

- Java 21 o superior
- Maven 3.8+
- Postman (para testing)
- Cuenta de Gmail con App Password (para envío de emails)
- GitHub Personal Access Token con permisos de `model`

### 2. Configurar Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto (copia `.env.example`):
```bash
# GitHub Models
GITHUB_TOKEN=ghp_tu_token_personal_de_github
GITHUB_MODELS_URL=https://models.inference.ai.azure.com/chat/completions
MODEL_ID=gpt-4o-mini

# JWT
JWT_SECRET=tu-clave-super-secreta-jwt-cambiar-en-produccion

# Email (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password-de-gmail
```

**⚠️ Cómo obtener App Password de Gmail:**
1. Ve a tu cuenta de Google → Seguridad
2. Activa verificación en 2 pasos
3. Busca "Contraseñas de aplicaciones"
4. Genera una contraseña para "Correo"
5. Usa esa contraseña en `MAIL_PASSWORD`

### 3. Ejecutar el Proyecto
```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### 4. Acceder a H2 Console (Debugging)

URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:oreo_db`
- Username: `sa`
- Password: (dejar vacío)

---

## 🧪 Ejecutar Tests
```bash
# Todos los tests
mvn test

# Solo tests del servicio de agregación
mvn test -Dtest=SalesAggregationServiceTest
```

**Cobertura de Tests**:
- ✅ 5+ test cases para SalesAggregationService
- ✅ Cubre casos normales, lista vacía, filtros y edge cases

---

## 📮 Ejecutar Postman Collection

### Opción 1: Postman Desktop

1. Abre Postman
2. Importa el archivo `postman_collection.json`
3. Verifica que la variable `baseUrl` sea `http://localhost:8080`
4. Ejecuta la colección completa con "Run Collection"
5. Todos los tests deben pasar ✅

### Opción 2: Newman (CLI)
```bash
npm install -g newman
newman run postman_collection.json
```

---

## 🔄 Implementación Asíncrona

### Arquitectura de Eventos

El sistema usa **Spring Events** con `@Async` para procesamiento asíncrono:

Controller → Publica ReportRequestedEvent → Retorna 202 Accepted
↓
@EventListener + @Async
↓
ReportEventListener procesa en background:
1. Calcula agregados (SalesAggregationService)
2. Genera resumen con LLM (LLMService)
3. Envía email (EmailService o PremiumEmailService)

### Configuración Async
```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("oreo-async-");
        executor.initialize();
        return executor;
    }
}
```

### Ventajas de Este Enfoque

- ✅ **No bloquea** el thread principal
- ✅ **Escalable**: Pool de threads configurable
- ✅ **Desacoplado**: Controller no conoce la lógica de negocio
- ✅ **Testeable**: Componentes independientes
- ✅ **Resiliente**: Fallos no afectan la respuesta HTTP

---

## 🎯 BONUS: Reportes Premium

### Endpoint Premium
```http
POST /sales/summary/weekly/premium
Authorization: Bearer {token}
Content-Type: application/json

{
  "from": "2025-09-01",
  "to": "2025-09-07",
  "emailTo": "gerente@oreo.com",
  "format": "PREMIUM",
  "includeCharts": true,
  "attachPdf": true
}
```

### Características del Reporte Premium

1. **Email HTML profesional** con estilos CSS inline
2. **Gráficos embebidos** usando QuickChart.io
3. **PDF adjunto** generado con iText7
4. **Procesamiento asíncrono** igual que reportes normales

### Ejemplo de Email Premium
```html
<!DOCTYPE html>
<html>
<head>
  <style>
    .header { background: linear-gradient(135deg, #6B46C1 0%, #805AD5 100%); }
    .metric { display: inline-block; padding: 20px; background: #f0f0f0; }
  </style>
</head>
<body>
  <div class="header">
    <h1>🍪 Reporte Premium Oreo</h1>
  </div>
  <div class="content">
    <!-- Resumen ejecutivo + métricas + gráficos -->
  </div>
</body>
</html>
```

---

## 📊 Endpoints Disponibles

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Registrar usuario | No |
| POST | `/auth/login` | Iniciar sesión | No |

### Ventas

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/sales` | Crear venta | CENTRAL, BRANCH |
| GET | `/sales/{id}` | Ver venta | CENTRAL, BRANCH |
| GET | `/sales` | Listar ventas | CENTRAL, BRANCH |
| PUT | `/sales/{id}` | Actualizar venta | CENTRAL, BRANCH |
| DELETE | `/sales/{id}` | Eliminar venta | CENTRAL |

### Reportes

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/sales/summary/weekly` | Reporte normal (async) | CENTRAL, BRANCH |
| POST | `/sales/summary/weekly/premium` | Reporte premium (async) | CENTRAL, BRANCH |

### Usuarios

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/users` | Listar usuarios | CENTRAL |
| GET | `/users/{id}` | Ver usuario | CENTRAL |
| DELETE | `/users/{id}` | Eliminar usuario | CENTRAL |

---

## 🔐 Seguridad

### Roles y Permisos

**ROLE_CENTRAL**:
- Acceso completo a todas las ventas
- Puede crear/modificar ventas de cualquier sucursal
- Puede eliminar ventas
- Puede gestionar usuarios

**ROLE_BRANCH**:
- Solo ve ventas de su sucursal asignada
- Solo puede crear ventas para su sucursal
- No puede eliminar ventas
- No puede gestionar usuarios

### JWT

- **Expiración**: 1 hora (3600 segundos)
- **Algoritmo**: HS256
- **Claims**: username, role, branch

---

## 🐛 Manejo de Errores

Todos los errores siguen el formato estándar:
```json
{
  "error": "ERROR_CODE",
  "message": "Mensaje descriptivo",
  "timestamp": "2025-10-21T10:00:00Z",
  "path": "/endpoint"
}
```

**Códigos HTTP**:
- `201`: Recurso creado
- `202`: Aceptado (async)
- `200`: OK
- `204`: Sin contenido
- `400`: Bad request
- `401`: No autenticado
- `403`: Sin permisos
- `404`: No encontrado
- `409`: Conflicto (duplicado)
- `503`: Servicio no disponible

---

## 📝 Notas Importantes

1. **Base de datos H2**: Se reinicia cada vez que paras la aplicación
2. **Emails**: Verifica tu carpeta de SPAM si no ves los reportes
3. **GitHub Models**: El token debe tener permisos de `model`
4. **Testing local**: Usa MailDev o Mailtrap para emails de prueba
5. **Producción**: Cambiar H2 por PostgreSQL y usar secretos reales

---

## 🏆 Funcionalidades Implementadas

### Requisitos Obligatorios ✅
- [x] Autenticación JWT con roles CENTRAL y BRANCH
- [x] CRUD completo de ventas con permisos por sucursal
- [x] Procesamiento asíncrono con @Async y @EventListener
- [x] Integración con GitHub Models para LLM
- [x] Envío de emails con resúmenes semanales
- [x] 5+ tests unitarios para SalesAggregationService
- [x] Postman Collection funcional end-to-end
- [x] Validaciones completas