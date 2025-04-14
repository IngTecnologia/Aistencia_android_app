# 📱 Aplicación de Verificación de Asistencia con Reconocimiento Facial

![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.8%2B-orange?logo=kotlin)
![Licencia](https://img.shields.io/badge/Licencia-INEMEC-red)
![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-blue)

Esta aplicación Android permite a los empleados registrar su asistencia de forma segura mediante reconocimiento facial y geolocalización, conectándose con nuestra API propietaria de verificación biométrica.

---

## 🚀 Características principales

- 🧠 **Verificación biométrica facial**: Utiliza la cámara del dispositivo para capturar una imagen facial y verificar la identidad del usuario contra nuestra base de datos.
- 📍 **Validación de ubicación GPS**: Garantiza que el usuario se encuentra en su lugar de trabajo autorizado.
- 🔄 **Proceso en dos pasos**: Verificación secuencial (ubicación + reconocimiento facial).
- 🖥️ **Interfaz intuitiva**: Diseño sencillo y guiado para una experiencia de usuario fluida.
- 🤝 **Funcionamiento sin contacto**: No requiere contacto físico, a diferencia de sistemas tradicionales de huella digital.
- 🔐 **Seguridad avanzada**: Utiliza tokens JWT para gestionar la sesión de forma segura.

---

## 🧩 Requisitos técnicos

- 📱 Android 5.0 (API 21) o superior
- 🎥 Permisos de cámara y ubicación
- 🌐 Conexión a internet

---

## 🏗️ Arquitectura

La aplicación está desarrollada utilizando:

- 🧑‍💻 **Kotlin** como lenguaje principal
- 🧩 **Arquitectura de Fragmentos** para mejor usabilidad
- 🌐 **Retrofit** para comunicación con la API
- 🎥 **CameraX** para gestión de la cámara
- 📍 **Google Play Services** para ubicación precisa
- 🎨 **Material Design** para la interfaz de usuario

---

## 🔄 Flujo de la aplicación

1. 🔑 **Inicio de sesión**: El usuario ingresa su número de cédula y selecciona el tipo de registro (entrada/salida).
2. 📍 **Verificación de ubicación**: La aplicación verifica que el usuario se encuentre dentro del radio permitido.
3. 🤳 **Captura facial**: El usuario toma una fotografía de su rostro.
4. 🔎 **Verificación**: La imagen se envía a la API para ser comparada con la imagen base.
5. ✅ **Resultado**: Se muestra el resultado de la verificación y se registra la asistencia en caso de éxito.

---

## 🔒 Seguridad

- 🔐 Toda la comunicación con la API se realiza a través de conexiones seguras (HTTPS).
- ⏱️ Los tokens de sesión tienen caducidad temporal.
- 🗑️ Las imágenes capturadas no se almacenan permanentemente en el dispositivo.
- 🧩 La aplicación valida la integridad de las respuestas del servidor.

---

## 🏢 Integración con sistemas empresariales

La aplicación forma parte de un ecosistema completo de control de asistencia que se integra con:

- 🧾 **Sistemas de nómina**
- 👥 **Software de gestión de recursos humanos**
- 📊 **Paneles administrativos** para generación de informes

---

## 🎨 Personalización

La aplicación puede personalizarse con:

- 🖼️ Logos corporativos
- 🎨 Colores de marca
- 🌍 Configuraciones específicas de geolocalización
- 🔌 Integración con sistemas propios de cada empresa

---

## 👤 Desarrollado por

**INEMEC - Soluciones Tecnológicas Empresariales**

Desarrollo liderado por **Ing. Jesús Cotes**

[![GitHub](https://img.shields.io/badge/GitHub-@IngTecnologia-black?logo=github)](https://github.com/IngTecnologia)

---

## ⚙️ Instalación para desarrollo

```bash
# Clonar el repositorio
git clone https://github.com/IngTecnologia/NombreDelRepositorio.git

# Abrir el proyecto en Android Studio

# Configurar la URL de la API en ApiClient.kt

# Compilar y ejecutar en un dispositivo o emulador
```

---

## 🗺️ Roadmap

- [ ] Implementación de autenticación biométrica integrada con el dispositivo
- [ ] Soporte para trabajo remoto con validación de ubicaciones múltiples
- [ ] Notificaciones push para recordatorios de registro
- [ ] Modo offline con sincronización posterior

---

## 📄 Licencia

Este proyecto es propiedad de **INEMEC**.  
**Todos los derechos reservados.**

---

## 👤 Autor

Desarrollado por **Ing. Jesús Cotes**

[![GitHub](https://img.shields.io/badge/GitHub-@IngTecnologia-black?logo=github)](https://github.com/IngTecnologia)

---
