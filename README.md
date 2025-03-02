# Sistema POS para Procesamiento de Pagos

Este proyecto implementa un terminal Punto de Venta (POS) para el procesamiento de pagos con tarjetas de crédito, similar a dispositivos físicos como DataFast. El sistema forma parte de un ecosistema de pagos que incluye Payment Gateway, Payment Processor, entidades emisoras de tarjetas y Core Banking System.

## Características

- Procesamiento de transacciones corrientes
- Procesamiento de transacciones diferidas (pagos a plazos)
- Procesamiento de transacciones recurrentes (suscripciones)
- Comunicación con el Payment Gateway para autorización de transacciones
- Base de datos SQLite para almacenamiento local de transacciones

## Tecnologías

- Java 21
- Spring Boot 3.4.3
- Spring Data JPA
- Spring Cloud OpenFeign
- SQLite
- MapStruct
- Lombok
- OpenAPI (Swagger)

## Estructura del Proyecto

```
com.banquito.pos/
├── config/                 # Configuraciones (CORS, OpenAPI)
├── client/                 # Cliente Feign para comunicación con Payment Gateway
│   └── dto/                # DTOs para comunicación con servicios externos
├── controller/             # Controladores REST
│   ├── dto/                # DTOs para las API
│   └── mapper/             # Mappers para conversión entre entidades y DTOs
├── exception/              # Excepciones personalizadas
├── model/                  # Entidades JPA
├── repository/             # Repositorios JPA
└── service/                # Servicios de negocio
```

## Modelo de Datos

### POS_CONFIGURACION
- MODELO (PK)
- CODIGO_POS (PK)
- DIRECCION_MAC
- CODIGO_COMERCIO
- FECHA_ACTIVACION

### POS_TRANSACCION
- COD_TRANSACCION (PK)
- TIPO
- MARCA
- MODALIDAD
- DETALLE
- MONTO
- CODIGO_UNICO_TRANSACCION
- FECHA
- ESTADO
- ESTADO_RECIBO
- MONEDA

## Endpoints API

### Configuración del Terminal

- `GET /v1/configuracion` - Obtener configuración actual del terminal POS
- `POST /v1/configuracion` - Configurar el terminal POS

### Transacciones

- `POST /v1/transacciones` - Procesar transacción en el terminal POS

## Formato de Transacción

El JSON enviado al endpoint `/v1/transacciones` determina el tipo de transacción a procesar:

```json
{
  "tipo": "COM",                        // COM (Compra), DEV (Devolución), ANU (Anulación)
  "marca": "VISA",                      // VISA, MAST, AMEX, DISC
  "modalidad": "COR",                   // COR (Corriente), DIF (Diferido), REC (Recurrente)
  "monto": 125.50,                      
  "moneda": "USD",                      // USD, EUR, MXN
  "numeroTarjeta": "4111111111111111",
  "nombreTitular": "Juan Pérez",
  
  // Para transacciones diferidas (modalidad = "DIF")
  "plazo": 6,                           // Número de meses
  
  // Para transacciones recurrentes (modalidad = "REC")
  "recurrente": true,
  "frecuenciaDias": 30                  // Frecuencia en días
}
```

## Ejecución

Para ejecutar la aplicación:

```bash
mvn spring-boot:run
```

## Documentación de la API

La documentación de la API está disponible en:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Configuración

La configuración principal se encuentra en `application.properties`. Para comunicación con el Payment Gateway, se configura la URL en esta propiedad: `payment-gateway.url`. 