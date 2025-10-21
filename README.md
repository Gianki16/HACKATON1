🍪 Hackathon #1: Oreo Insight Factory
Descripción General
Backend para la fábrica Oreo con UTEC. Permite registrar ventas y generar resúmenes automáticos vía LLM, enfocado en calidad de API, autenticación JWT, persistencia y testing. Validado con Postman Flow end-to-end: login, seed de ventas, queries y /summary.

Tecnologías Obligatorias :
- Java 21+ 
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA
- H2/PostgreSQL
- GitHub Models API Client/SDK
- JavaMail/Spring Boot Mail
- @Async y @EventListener para procesamiento asíncrono

Roles y Seguridad
ROLE_CENTRAL: Acceso total, reportes globales, gestión de usuarios y ventas de todas las sucursales.

ROLE_BRANCH: Acceso solo a ventas propias, solo puede crear/ver ventas de sucursal asignada.

Cada usuario con ROLE_BRANCH debe tener una sucursal asignada al registrarse.

Autenticación y autorización usando JWT. Todos los endpoints protegidos requieren token válido.

Endpoints Principales
1. Autenticación JWT
   Método  |  Endpoint        |  Descripción    |  Roles  
   --------+------------------+-----------------+---------
   POST    |  /auth/register  |  Crear usuario  |  Público
   POST    |  /auth/login     |  Obtener JWT    |  Público

Validaciones:
- username: 3-30 caracteres
- email: formato válido
- password: mínimo 8 caracteres
- role: CENTRAL o BRANCH
- branch: obligatorio si es BRANCH

Ejemplo Registro:
{
"username": "oreo.admin",
"email": "admin@oreo.com",
"password": "Oreo1234",
"role": "CENTRAL"
}

Ejemplo Login:
{
"username": "oreo.admin",
"password": "Oreo1234"
}

2. Gestión de Ventas
   Método  |  Endpoint     |  Descripción              |  Roles                     |  Body/Query        |  Response
   --------+---------------+---------------------------+----------------------------+--------------------+----------
   POST    |  /sales       |  Crear venta              |  CENTRAL, BRANCH (propia)  |  JSON ejemplo      |  201     
   GET     |  /sales/{id}  |  Ver venta                |  CENTRAL, BRANCH (propia)  |  -                 |  200     
   GET     |  /sales       |  Listar ventas (filtros)  |  CENTRAL, BRANCH (propia)  |  from, to, branch  |  200     
   PUT     |  /sales/{id}  |  Actualizar venta         |  CENTRAL                   |  JSON ejemplo      |  200     
   DELETE  |  /sales/{id}  |  Eliminar venta           |  CENTRAL                   |  -                 |  204     

Ejemplo Creación:
{
"sku": "OREO_CLASSIC_12",
"units": 25,
"price": 1.99,
"branch": "Miraflores",
"soldAt": "2025-09-12T16:30:00Z"
}


3. Resumen Semanal Asíncrono con LLM y Email
   Método  |  Endpoint               |  Descripción                            |  Roles          
   --------+-------------------------+-----------------------------------------+-----------------
   POST    |  /sales/summary/weekly  |  Generación asíncrona de summary+email  |  CENTRAL, BRANCH

Request ejemplo:
{
"from": "2025-09-01",
"to": "2025-09-07",
"branch": "Miraflores",
"emailTo": "gerente@oreo.com"
}

Response inmediato (202 Accepted):
{
"requestId": "req_01k...",
"status": "PROCESSING",
"message": "Su solicitud está siendo procesada. Recibirá el resumen en gerente@oreo.com en breve.",
"estimatedTime": "30-60 segundos",
"requestedAt": "2025-09-12T18:15:00Z"
}

4. Gestión de Usuarios (Solo CENTRAL)

Método  |  Endpoint     |  Descripción       |  Roles  
--------+---------------+--------------------+---------
GET     |  /users       |  Listar usuarios   |  CENTRAL
GET     |  /users/{id}  |  Ver usuario       |  CENTRAL
DELETE  |  /users/{id}  |  Eliminar usuario  |  CENTRAL

Implementación Asíncrona
Flujo:
1. Controller recibe petición y retorna 202 Accepted. 
2. Publica evento ReportRequestedEvent. 
3. Listener con @EventListener y @Async procesa:
   - Calcula agregados 
   - Consulta LLM (GitHub Models)
   - Genera resumen y lo envía por email 
   - Actualiza status en BD
Ejemplo:   
     @Async
     @EventListener
     public void handleReportRequest(ReportRequestedEvent event) {
     // 1. Calcular agregados
     // 2. Llamar a GitHub Models
     // 3. Enviar email
     // 4. Actualizar estado
     }

Testing Unitario
Implementa mínimo 5 tests para el servicio de agregados:
  - totalUnits, totalRevenue, topSKU, topBranch con datos válidos 
  - Sin ventas en rango de fechas 
  - Filtrado por sucursal 
  - Filtrado por fechas 
  - SKU top cuando hay empates

Ejemplo:
@ExtendWith(MockitoExtension.class)
class SalesAggregationServiceTest { 
    @Mock private SalesRepository salesRepository; 
    @InjectMocks private SalesAggregationService salesAggregationService;

    @Test
    void shouldCalculateCorrectAggregatesWithValidData() {
        List<Sale> mockSales = List.of(
            createSale("OREO_CLASSIC", 10, 1.99, "Miraflores"),
            createSale("OREO_DOUBLE", 5, 2.49, "San Isidro")
        );
        when(salesRepository.findByDateRange(any(), any())).thenReturn(mockSales);
        SalesAggregates result = salesAggregationService.calculateAggregates(...);
        // asserts aquí
    }
}

















































