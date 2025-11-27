# Sistema de Reportes con JasperReports - SysAcopio

## 📄 Descripción

El sistema de acopio ahora cuenta con un sistema robusto de generación e impresión de reportes usando **JasperReports**. Este sistema permite visualizar, exportar e imprimir vouchers de acopio en múltiples formatos.

## ✨ Funcionalidades

### 1. Visualización de Reportes
- **Visor integrado**: Muestra los vouchers en una ventana interactiva de JasperReports
- **Vista previa**: Permite revisar el documento antes de exportar

### 2. Exportación Multi-formato
- **PDF**: Exporta vouchers para distribución y archivo
- **Excel (XLSX)**: Exporta datos para análisis en hojas de cálculo
- **Timestamps automáticos**: Los archivos exportados incluyen fecha y hora

### 3. Gestión Optimizada
- **Pre-compilación**: Los reportes .jrxml se compilan a .jasper para mejor rendimiento
- **Caché en memoria**: Los reportes compilados se mantienen en memoria
- **Carpeta jasper/**: Almacena versiones pre-compiladas para acceso rápido

## 🚀 Cómo Usar

### Generar y Ver un Voucher

1. **Registrar un Acopio**: Completa el formulario de acopio con proveedor, materiales y detalles
2. **Guardar**: Haz clic en "Guardar Acopio"
3. **Confirmación de Impresión**: Se te preguntará si deseas imprimir el voucher
4. **Visualizar**: El voucher se abre en el visor de JasperReports

### Exportar un Voucher

Después de visualizar el voucher:

1. **Diálogo de Exportación**: Se te pregunta si deseas exportar
2. **Seleccionar Formato**: Elige entre PDF o Excel
3. **Seleccionar Ubicación**: Usa el explorador de archivos para elegir dónde guardar
4. **Confirmación**: Recibes un mensaje con la ruta del archivo guardado

### Estructura de Archivos

```
SysAcopio/
├── src/main/resources/reports/
│   └── comprobante_acopio.jrxml    # Plantilla del reporte
├── jasper/
│   └── comprobante_acopio.jasper   # Reporte pre-compilado (generado automáticamente)
├── src/main/java/pe/com/acopio/
│   ├── service/
│   │   ├── AcopioService.java           # Servicio de acopio (usa JasperReportService)
│   │   └── JasperReportService.java     # Servicio centralizado de reportes ⭐ NUEVO
│   ├── controller/
│   │   └── AcopioController.java        # Controlador con métodos de exportación
│   └── util/
│       └── ReportAlert.java             # Utilidades de diálogos mejoradas
```

## 🛠 Componentes Técnicos

### JasperReportService

Servicio centralizado que gestiona todo lo relacionado con reportes:

```java
@Autowired
private JasperReportService jasperReportService;

// Compilar y llenar reporte
JasperPrint jasperPrint = jasperReportService.fillReport(
    "comprobante_acopio", 
    parameters, 
    dataSource
);

// Exportar a PDF
jasperReportService.exportToPDF(jasperPrint, "ruta/archivo.pdf");

// Exportar a Excel
jasperReportService.exportToExcel(jasperPrint, "ruta/archivo.xlsx");
```

### Métodos Principales

#### En AcopioController:
- `handleImprimirVoucher()`: Genera y muestra el voucher
- `handleExportarVoucher()`: Gestiona el proceso de exportación
- `exportarAPDF()`: Exporta a formato PDF
- `exportarAExcel()`: Exporta a formato Excel

#### En JasperReportService:
- `compileReport()`: Compila un .jrxml a JasperReport
- `fillReport()`: Genera un JasperPrint con datos
- `exportToPDF()`: Exporta a PDF
- `exportToExcel()`: Exporta a Excel
- `exportToHTML()`: Exporta a HTML
- `viewReport()`: Muestra en visor

## 📦 Dependencias

El sistema requiere las siguientes dependencias en `pom.xml`:

```xml
<!-- JasperReports -->
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.20.6</version>
</dependency>

<!-- Apache POI para Excel -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.3</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

## 🎨 Personalizar Reportes

Para modificar el diseño del voucher:

1. **Editar JRXML**: Modifica `src/main/resources/reports/comprobante_acopio.jrxml`
2. **Usar JasperSoft Studio**: Herramienta visual para diseñar reportes
3. **Recompilar**: El sistema recompilará automáticamente al detectar cambios

### Parámetros del Reporte

El voucher de acopio usa estos parámetros:

- `numeroAcopio`: Número único del acopio
- `fechaAcopio`: Fecha del acopio
- `proveedorNombre`: Nombre completo del proveedor
- `proveedorDocumento`: DNI/RUC del proveedor
- `proveedorDireccion`: Dirección del proveedor
- `usuarioNombre`: Nombre del usuario que registró
- `totalPagar`: Total a pagar en soles
- `observaciones`: Observaciones adicionales

### Campos del Detalle

Cada ítem del acopio incluye:

- `numeroItem`: Número correlativo
- `material.nombre`: Nombre del material
- `peso`: Peso en gramos
- `ley`: Ley del material (%)
- `deduccion`: Deducción aplicada
- `precioOnzaBase`: Precio por onza en dólares
- `tipoCambioDolar`: Tipo de cambio USD -> PEN
- `precioGramoSoles`: Precio por gramo en soles
- `totalAPagar`: Total a pagar por este ítem

## 🔍 Solución de Problemas

### El reporte no se compila

**Problema**: Error al compilar el archivo JRXML

**Solución**:
1. Verifica que el archivo `comprobante_acopio.jrxml` existe en `src/main/resources/reports/`
2. Verifica que el XML está bien formado
3. Revisa los logs para ver el error específico

### Los archivos no se exportan

**Problema**: No se genera el PDF o Excel

**Solución**:
1. Verifica que tienes permisos de escritura en la carpeta destino
2. Asegúrate de que las dependencias de Apache POI están instaladas
3. Revisa los logs para errores específicos

### El visor no se muestra

**Problema**: La ventana de JasperViewer no aparece

**Solución**:
1. Verifica que JavaFX está correctamente configurado
2. Asegúrate de que no hay bloqueos de ventanas emergentes
3. Revisa si hay excepciones en la consola

## 📝 Logs

El sistema registra todas las operaciones de reportes:

```
INFO  - Generando voucher para acopio ID: 123
INFO  - Voucher generado exitosamente para acopio: ACO-2025-0001
INFO  - Exportando reporte a PDF: C:/Users/Usuario/Documents/Voucher_123.pdf
INFO  - Reporte exportado exitosamente a PDF
```

## 🎯 Mejoras Futuras

### Reportes y Análisis
- [ ] **Reportes consolidados por periodo** - Resúmenes mensuales/anuales de acopios
- [ ] **Gráficos y estadísticas** - Visualización de tendencias y KPIs
- [ ] Reportes comparativos entre proveedores
- [ ] Dashboard ejecutivo con métricas clave

### Personalización
- [ ] **Plantillas personalizables por usuario** - Cada usuario con su formato preferido
- [ ] Temas visuales para reportes (claro/oscuro)
- [ ] Logos y encabezados configurables
- [ ] Campos personalizados en vouchers

### Distribución
- [ ] **Envío automático por email** - Enviar vouchers a proveedores
- [ ] **Firma digital de documentos** - Firmas electrónicas válidas
- [ ] Exportación a otros formatos (Word, CSV, JSON)
- [ ] ~~Impresión directa sin vista previa~~ ✅ **Implementado**

### Seguridad y Auditoría  
- [ ] Registro de impresiones y exportaciones
- [ ] Control de versiones de reportes
- [ ] Marca de agua con datos de auditoría

## 👥 Soporte

Para problemas o consultas sobre el sistema de reportes:

- Revisa los logs de la aplicación
- Consulta la documentación de JasperReports: https://community.jaspersoft.com/
- Revisa el código fuente en `JasperReportService.java`

---

**Nota**: Este sistema usa JasperReports 6.20.6 y Apache POI 5.2.3. Asegúrate de tener estas versiones instaladas para compatibilidad óptima.
