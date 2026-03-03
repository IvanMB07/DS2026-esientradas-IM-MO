🅰️ Guía de Angular ESIFE - 2026
🛠️ 1. Requisitos e Instalación Inicial
Instalar Node.js: Descarga la versión LTS en nodejs.org.

Permisos PowerShell: Ejecuta Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser.

Instalar Angular CLI: Ejecuta npm install -g @angular/cli en la terminal.

Verificar: Usa node -v y ng version para confirmar que todo esté OK.

🚀 2. Creación y Ejecución del Proyecto
Nuevo Proyecto: ng new esife (Responde Y a Routing y elige CSS/SCSS).

Entrar a la carpeta: cd esife

Lanzar Servidor: ng serve --open (Abre el proyecto en http://localhost:4200).

Detener: Ctrl + C en la terminal.

📂 3. Estructura y Comandos Útiles
src/app/: Donde sucede la magia (componentes y lógica).

package.json: El "DNI" del proyecto (lista de dependencias).

Generar Componente: ng g c nombre-componente

Generar Servicio: ng g s nombre-servicio

Instalar Librería: npm install nombre-libreria

💻 4. Transferir a otro Ordenador (¡IMPORTANTE!)
[!CAUTION]
NUNCA copies la carpeta node_modules. Es pesada y da errores de compatibilidad.

En el PC origen: Copia todo excepto node_modules/, .angular/ y dist/.

En el PC destino: Abre la terminal en la carpeta y ejecuta npm install.

Ejecutar: Una vez descargadas las dependencias, lanza con ng serve.

⚠️ 5. Solución de Problemas Rápidos
"ng no se reconoce": Reinstala el CLI con npm install -g @angular/cli.

Puerto 4200 ocupado: Usa ng serve --port 4201.

Errores extraños: Borra node_modules y package-lock.json, luego haz npm install.

Cambios no se ven: El servidor tiene Hot Reload, pero si falla, reinicia el comando ng serve.

💡 Tips para el Proyecto ESIFE
Git: Usa un archivo .gitignore para no subir basura al repositorio.

Assets: Guarda tus imágenes en src/assets/ para que Angular las reconozca.

Documentación: Consulta angular.io para ejemplos de sintaxis avanzada.