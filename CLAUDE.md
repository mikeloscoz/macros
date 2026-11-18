# App de calorías (Android)

App personal para contar calorías y macros. Sustituye a una hoja de cálculo que uso a diario.
Prioridades: sencilla, ligera, fluida y sin nada que agobie. Uso personal: sin cuentas, sin servidor,
sin anuncios, sin analítica y sin permisos de red. Toda la interfaz va en castellano.

## Stack

- Kotlin + Jetpack Compose + Material 3
- Room para los datos (todo se guarda en el móvil)
- DataStore (Preferences) para los ajustes
- Vico para las gráficas
- Navigation Compose con barra inferior de 4 pestañas
- Un solo módulo `app`, arquitectura simple: pantalla → ViewModel → repositorio → Room
- minSdk 26. Gradle con Kotlin DSL y version catalog (`libs.versions.toml`)

## Datos

- **Alimento**: nombre (único), kcal, carbohidratos, proteínas y grasas, todo por 100 g.
- **Entrada del diario**: fecha, número de comida (1 a 4), alimento, gramos.
  Los valores se calculan como `valor_por_100g * gramos / 100`.
- **Peso**: fecha y kg (como mucho uno por día; si se registra otra vez el mismo día, se sobrescribe).
- **Ajustes**: calorías de mantenimiento, objetivo (Perder grasa / Mantener / Ganar masa) y sexo (Hombre / Mujer).
- **Peso actual** = el último peso registrado. No se guarda aparte: cambiarlo en Ajustes registra el peso de hoy.
  Si no hay ninguno registrado, la app pide el peso la primera vez que se abre.
- La primera vez que se abre la app, cargar los alimentos de `app/src/main/assets/alimentos.csv`
  (columnas: Alimento, Kcal, Carbohidratos, Proteínas, Grasas; puede usar coma decimal).

## Cálculo de objetivos diarios

- Calorías: mantenimiento − 300 (Perder grasa), + 300 (Ganar masa), igual (Mantener).
- Proteínas: 2,2 g/kg (Hombre) o 1,8 g/kg (Mujer), con el peso actual.
- Grasas: 0,8 g/kg (Hombre) o 1,2 g/kg (Mujer).
- Carbohidratos: lo que sobra → `max(0, (kcal − 4·proteínas − 9·grasas) / 4)`.

Ejemplo para comprobar: 82 kg, 2300 de mantenimiento, Perder grasa, Hombre → 2000 kcal, 172 g carbohidratos, 180 g proteínas, 66 g grasas.

## Pantallas

Las capturas del diseño aprobado están en `design/`. Hay que seguirlas lo más fielmente posible.
Los números de las capturas son de ejemplo (los de Progreso, inventados): en la app salen de los datos reales.
Las kcal y los gramos se muestran redondeados a enteros; el peso con un decimal.

1. **Hoy**: fecha, tarjeta resumen con anillo de kcal restantes (consumidas / objetivo) y tres barras
   (carbohidratos, proteínas, grasas) con "Te quedan X g". Debajo, 4 tarjetas "Comida 1…4" con sus alimentos
   (nombre, gramos, kcal), total de la comida y botón "Añadir alimento" (buscador de alimentos + gramos).
   Tocar un alimento permite cambiar los gramos o borrarlo. Si te pasas del objetivo, el restante se muestra en rojo.
   Permitir cambiar de día (ayer, etc.) para ver o corregir días anteriores.
2. **Alimentos**: buscador, lista en orden alfabético (nombre, kcal y C/P/G por 100 g), botón "Nuevo",
   editar y borrar alimentos.
3. **Progreso**: selector Semana / Mes / Año. Tarjeta de peso (actual, cambio desde el primer registro, gráfica de línea),
   media de kcal por semana frente al objetivo (barras) y datos rápidos (días registrados, proteína media, kg por semana).
   Las medias cuentan solo los días que tienen algo apuntado. Si aún no hay datos, mostrar un mensaje en vez de gráficas vacías.
   Registrar el peso del día desde aquí.
4. **Ajustes**: mis datos, objetivos diarios calculados (solo lectura), copia de seguridad: exportar e importar todo
   en un JSON (selector de archivos del sistema) e importar alimentos desde CSV.

## Diseño

- Fuente Inter, incluida en la app como archivos en `res/font` (descárgala de Google Fonts; no uses fuentes descargables en tiempo de ejecución).
- Colores: primario `#1B7F4B`, texto `#12261B`, texto secundario `#566860`, fondo `#F4F9F6`,
  tarjetas `#FFFFFF` con borde `#DCEBE2`, verde claro `#E4F2EA`, rojo (te has pasado) `#B42318`.
  Macros: carbohidratos `#B86E00`, proteínas `#1B7F4B`, grasas `#2F6FB0`.
- Tarjetas con esquinas de 20 dp y márgenes laterales de 16 dp. Zonas táctiles de 44 dp como mínimo.
- Solo modo claro por ahora. Animaciones suaves y discretas.

## Forma de trabajar

- Pasos pequeños: una funcionalidad cada vez. Antes de cambios grandes, propón un plan corto.
- Después de cada cambio, compila con `.\gradlew.bat assembleDebug` y corrige los errores antes de terminar.
- No añadas librerías que no hagan falta ni funcionalidades que no estén aquí sin preguntar.
- No hagas commits ni push: los hago yo después de probar cada fase.
- El proyecto se llama Macros (paquete `com.example.macros`). Uso Windows con PowerShell: usa `.\gradlew.bat`.
- Explícame en castellano qué has cambiado y cómo probarlo.