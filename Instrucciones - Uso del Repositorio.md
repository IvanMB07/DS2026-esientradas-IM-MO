📘 Guía de Uso del Repositorio DS2026 (Paso a Paso)

Esta guía está pensada para alguien que no maneja mucho Git ni proyectos con varios servicios.
Aquí tienes qué abrir, en qué orden arrancar cada proyecto y qué subir a GitHub.

---

## 1) ¿Qué hay en este repositorio?

Este repo contiene 3 proyectos que trabajan juntos:

1. `DS2026-esientradas-IM-MO` → Backend principal (Spring Boot, puerto 8080, MySQL).
2. `DS2026-esiusuarios-IM-MO` → Backend de usuarios/tokens (Spring Boot, puerto 8081).
3. `esife` → Frontend Angular (normalmente puerto 4200).

Relación entre ellos:
- Angular (`esife`) llama a `esientradas` en `http://localhost:8080`.
- `esientradas` llama a `esiusuarios` en `http://localhost:8081`.

---

## 2) Requisitos mínimos

- Java instalado (recomendado Java 21 para evitar problemas entre proyectos).
- Node.js LTS (para Angular).
- MySQL funcionando en local.
- VS Code (o IDE similar) y terminal PowerShell.

---

## 3) Arranque del entorno (orden recomendado)

### Ventana/Terminal A: `esiusuarios` (backend usuarios)
Desde la raíz del repo:

```powershell
mvn -f "DS2026-esiusuarios-IM-MO/pom.xml" spring-boot:run
```

Debe quedar escuchando en `http://localhost:8081`.

### Ventana/Terminal B: `esientradas` (backend entradas)
Desde la raíz del repo:

```powershell
mvn -f "DS2026-esientradas-IM-MO/pom.xml" spring-boot:run
```

Debe quedar escuchando en `http://localhost:8080`.

### Ventana/Terminal C: `esife` (frontend Angular)
Primera vez (o si cambian dependencias):

```powershell
cd esife
npm install
```

Para arrancar:

```powershell
npm start
```

(Si no funciona `npm start`, usa `ng serve --open`.)

---

## 4) Parar proyectos

- En cada terminal: `Ctrl + C`.
- Para volver a arrancar, repite el bloque anterior en el mismo orden.

---

## 5) Qué SÍ y qué NO subir a GitHub

### SÍ subir
- Código fuente (`src/...`).
- Configuración del proyecto (`pom.xml`, `package.json`, `angular.json`, etc.).
- Documentación (`.md`).

### NO subir (archivos generados)
- `target/`
- `*.class`
- `*.log`
- `.idea/`, `.vscode/`, `*.iml`
- En Angular, tampoco `node_modules/`, `.angular/`, `dist/`

El repo ya tiene `.gitignore` para evitar esto.

---

## 6) Flujo simple y seguro para trabajar con Git

> Regla de oro: **NO usar “Upload files” en GitHub web para subir proyectos completos**.
> Haz siempre los cambios desde local con Git.

### Paso a paso recomendado

1. Actualizar `main`:
```powershell
git switch main
git pull --ff-only origin main
```

2. Crear rama nueva para tu tarea:
```powershell
git switch -c feat/nombre-cambio
```

3. Ver qué cambió:
```powershell
git status
```

4. Añadir y confirmar cambios:
```powershell
git add -A
git commit -m "feat: descripcion corta"
```

5. Subir tu rama:
```powershell
git push -u origin feat/nombre-cambio
```

6. Abrir Pull Request en GitHub y hacer merge a `main`.

7. Después del merge:
```powershell
git switch main
git pull --ff-only origin main
```

---

## 7) Checklist rápido antes de hacer push

Ejecuta esto desde la raíz:

```powershell
git status
git ls-files | Select-String "target/|\.class$|\.log$"
```

Si el segundo comando no devuelve nada, vas bien.

---

## 8) Problemas típicos y solución rápida

### “No me deja cambiar de rama”
Tienes cambios locales sin guardar en Git.

```powershell
git stash push -u -m "wip temporal"
```

Luego cambias de rama, y si quieres recuperarlo:

```powershell
git stash list
git stash pop
```

### “mvn no se reconoce”
No tienes Maven en PATH. Opciones:
- Instalar Maven y reiniciar terminal.
- O ejecutar desde IDE con plugin Maven.

### “Errores raros en VS Code”
- Comprueba que estás en la rama correcta (`git branch --show-current`).
- Haz `git pull --ff-only origin main`.
- Reinicia VS Code si el análisis se quedó desfasado.

---

## 9) Recomendación de organización personal

Para no liarte:
- Deja 3 terminales fijas (usuarios, entradas, frontend).
- Usa siempre nombres claros de ramas: `feat/...`, `fix/...`, `chore/...`.
- Haz commits pequeños y frecuentes.
- Antes de subir: `git status` + checklist de artefactos.

---

Si tienes dudas, sigue esta norma:
**primero actualizar `main`, luego rama nueva, luego commit/push, luego PR**.
