# EcoTrack 🌿

**EcoTrack** es una aplicación Android diseñada para la gestión y seguimiento eficiente de residuos. Permite a los usuarios registrar diferentes tipos de materiales, visualizar estadísticas de recolección y generar reportes detallados para promover la sostenibilidad.

## 🚀 Características

*   **Gestión de Residuos**: Registro detallado de tipo, cantidad (kg), unidad de medida, ubicación, fecha y hora.
*   **Reportes Avanzados**: Generación de reportes en formatos **PDF** (usando iText7) y **CSV** (usando OpenCSV) con filtros por fecha y tipo.
*   **Dashboard Estadístico**: Visualización gráfica del impacto ambiental mediante gráficos interactivos (usando MPAndroidChart).
*   **Perfil de Usuario**:
    *   Edición de datos personales (Nombre, Email, Rol).
    *   Gestión de foto de perfil (Cambiar/Eliminar con persistencia de permisos).
    *   Cambio de contraseña segura.
*   **Tema Adaptativo**: Soporte completo para **Modo Claro** y **Modo Oscuro** (Material Design DayNight).
*   **Base de Datos Local**: Almacenamiento persistente y seguro con SQLite.

## 🛠️ Tecnologías y Librerías

*   **Lenguaje**: Java 8.
*   **Interfaz**: XML con Material Components.
*   **Base de Datos**: SQLite.
*   **Gráficos**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart).
*   **PDF**: [iText7](https://itextpdf.com/).
*   **CSV**: [OpenCSV](http://opencsv.sourceforge.net/).

## 📥 Instalación

Para clonar y ejecutar este proyecto localmente, sigue estos pasos:

### 1. Clonar el repositorio
Abre una terminal y ejecuta:
```bash
git clone https://github.com/JuanitooPi/EcoTrack.git
```

### 2. Abrir en Android Studio
1.  Abre Android Studio.
2.  Selecciona **File > Open**.
3.  Navega hasta la carpeta donde clonaste el proyecto y selecciónala.
4.  Espera a que Gradle sincronice las dependencias.

### 3. Ejecutar
*   Conecta un dispositivo físico o inicia un emulador.
*   Haz clic en el botón **Run** (icono de play verde) en la parte superior.

## 🔑 Credenciales de Prueba (Por defecto)
*   **Usuario**: `Operario ECOLIM`
*   **Contraseña**: `123456`

## 📄 Licencia
Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.

---
*Desarrollado con ❤️ para un futuro más verde.*
