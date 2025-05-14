# GESTOAPP - Instrucciones de Puesta en Marcha

Estimado Jesús,

Este documento proporciona las instrucciones necesarias para configurar y ejecutar la aplicación GESTOAPP en su equipo. Por favor, siga los pasos detallados a continuación para asegurar un funcionamiento correcto.

## Paso 1: Configuración del Entorno de Ejecución (VM Options para JavaFX)

Para que la aplicación pueda encontrar y utilizar los componentes de JavaFX, es necesario configurar los argumentos de la Máquina Virtual (VM) en su IDE.

1.  **Localice la carpeta `lib` de su JavaFX SDK.** Por ejemplo, si descomprimió el SDK en `C:\javafx-sdk-23`, la ruta será `C:\javafx-sdk-23\lib`.
2.  **En IntelliJ IDEA:**
    *   Vaya a la configuración de ejecución/debug del proyecto GESTOAPP.
    *   Busque la sección de "VM options".
    *   Añada la siguiente línea en VM, **reemplazando `"C:\javafx-sdk-23\lib"` con la ruta real a la carpeta `lib` de su JavaFX SDK**:

    ```
    --module-path "C:\javafx-sdk-23\lib" --add-modules javafx.controls,javafx.fxml
    ```
    
## Paso 2: Configuración de la Base de Datos en phpMyAdmin

Para el correcto funcionamiento del sistema de creación de nuevos usuarios y pedidos, es crucial que los identificadores (IDs) de estas tablas se generen automáticamente. Esto se configura mediante la opción `AUTO_INCREMENT`.

Siga estos pasos en phpMyAdmin para las tablas `usuarios` y `pedidos` dentro de su base de datos, `tienda`:

### Configurar AUTO_INCREMENT para la tabla `usuarios`:

1.  Abra phpMyAdmin en su navegador.
2.  Seleccione su base de datos `tienda` en el panel izquierdo.
3.  Seleccione la tabla `usuarios` de la lista de tablas.
4.  Vaya a la pestaña **"Estructura"**.
5.  Localice la columna `id` (o el nombre que tenga su clave primaria).
6.  A la derecha de la fila de la columna `id`, haga clic en el icono de **"Cambiar"** (✏️).
7.  En la página de modificación de la columna, busque y marque la casilla de verificación **"A_I"** (que significa AUTO_INCREMENT).
8.  Haga clic en el botón **"Guardar"**.

### Configurar AUTO_INCREMENT para la tabla `pedidos`:

1.  En phpMyAdmin, con la base de datos `tienda` seleccionada.
2.  Seleccione la tabla `pedidos` de la lista de tablas.
3.  Vaya a la pestaña **"Estructura"**.
4.  Localice la columna `id` (o el nombre que tenga su clave primaria).
5.  A la derecha de la fila de la columna `id`, haga clic en el icono de **"Cambiar"** (✏️).
6.  Marque la casilla de verificación **"A_I"** (AUTO_INCREMENT).
7.  Haga clic en el botón **"Guardar"**.

## Paso 3: Ejecutar la Aplicación

Si ha seguido todos los pasos correctamente, la aplicación GESTOAPP debería iniciarse y conectarse a la base de datos sin problemas.

Si encuentra alguna dificultad, no dude en consultar con el equipo de desarrollo.

Atentamente,

El Equipo de Desarrollo de GESTOAPP