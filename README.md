# MathHelper

**Решатель уравнений для 6 класса с пошаговым объяснением**

---

## Что умеет

- Решает уравнения с дробями, скобками, переменными
- Показывает решение по шагам — не просто ответ, а как к нему пришли
- Вставка дробей через отдельную форму
- История решений (сохраняется между запусками, до 100 штук)

---

## Сборка JAR

```bash
mvn clean package
```

Результат: `target/MathHelper.jar`

---

## Упаковка в EXE

### Что нужно заранее

1. **JDK 21** (включает jpackage)
2. **WiX Toolset v3.11** — https://github.com/wixtoolset/wix3/releases
   - После установки добавить в PATH: `C:\Program Files (x86)\WiX Toolset v3.11\bin`
3. **JavaFX JMOD 21.0.12** — скачать и распаковать в корень проекта:
   - https://download2.gluonhq.com/openjfx/21.0.12/openjfx-21.0.12_windows-x64_bin-jmods.zip
   - Распаковать в папку `javafx-jmods/` в корне проекта

### Подготовка

```bash
mvn clean package
mkdir target/jpackage-input
copy target\MathHelper.jar target\jpackage-input\
```

### Вариант 1 — установщик MSI (с ярлыком на рабочем столе)

```powershell
$env:Path = "C:\Program Files (x86)\WiX Toolset v3.11\bin;" + $env:Path

jpackage --name MathHelper --input target/jpackage-input --main-jar MathHelper.jar `
  --main-class ru.math.Launcher --type msi --dest target --app-version 2.0.0 `
  --vendor "MathHelper" --win-menu --win-shortcut --win-dir-chooser `
  --icon src/main/resources/ru/math/images/icon.ico `
  --runtime-image "$env:JAVA_HOME"
```

Результат: `target/MathHelper-2.0.0.msi` (~84 MB)

### Вариант 2 — портативная версия (папка с exe)

```powershell
jpackage --name MathHelper --input target/jpackage-input --main-jar MathHelper.jar `
  --main-class ru.math.Launcher --type app-image --dest target --app-version 2.0.0 `
  --vendor "MathHelper" --icon src/main/resources/ru/math/images/icon.ico `
  --runtime-image "$env:JAVA_HOME"
```

Результат: `target/MathHelper-2.0.0.msi` (~43 MB)

### Вариант 2 — портативная версия (папка с exe)

```powershell
jpackage --name MathHelper --input target/jpackage-input --main-jar MathHelper.jar `
  --main-class ru.math.Launcher --type app-image --dest target --app-version 2.0.0 `
  --vendor "MathHelper" --module-path "javafx-jmods" --add-modules javafx.controls,javafx.fxml
```

Результат: папка `target/MathHelper/` с `MathHelper.exe`

---

## Как передать другому человеку

- **MSI** — человек запускает, устанавливает, появляется ярлык на рабочем столе
- **Портативная папка** — скопировал папку, запустил `MathHelper.exe`, готово

Java устанавливать не нужно — всё уже внутри.

---

## Куда пишутся данные

Всё в домашней папке пользователя, а не в папке программы:

```
C:\Users\<Имя>\.uravnyashka\
├── logs\          ← логи
└── history.json   ← история решений (до 100 штук)
```

Поэтому программа работает откуда угодно — хоть из Program Files, хоть с флешки.

---

## Технологии

- Java 21 + JavaFX 21
- Maven (сборка)
- Lombok
- SLF4J + Logback (логирование)
- Jackson (сохранение истории)
- jpackage (упаковка в EXE)

---

## 📝 Примечания для разработки

### Почему нужен Launcher.java

Когда main class расширяет `javafx.application.Application`, Java при запуске `java -jar` проверяет: "О, это JavaFX-приложение! Дай-ка я его через JavaFX Launcher запущу". Но JavaFX в classpath (внутри fat JAR) не считается module path'ом — отсюда ошибка "JavaFX runtime components are missing".

**Launcher.java** — хитрый трюк: он **не наследуется** от Application, поэтому Java запускает его как обычный класс. А он уже внутри вызывает `MainApp.main(args)`, который и стартует JavaFX-окно.

### Зачем нужны JMOD-файлы

**JMOD-файлы** — это модули JavaFX в сыром виде (нативные DLL + Java-классы). Они нужны jpackage для создания установщика.

**Можно не оставлять**, если:
- Ты собираешь проект на своём компе через `--runtime-image "$env:JAVA_HOME"` — тогда JDK сам содержит всё нужное
- Или у тебя есть доступ к Maven Central, чтобы mvn подтянул JavaFX-зависимости

**Лучше оставить**, если:
- Ты хочешь, чтобы любой мог скачать проект с GitHub и сразу собрать MSI, не скачивая JavaFX отдельно
- У тебя могут быть перебои с интернетом (Maven Central в России сейчас недоступен)

### META-INF — что это и зачем

`META-INF/` — служебная папка внутри JAR. В ней лежит `MANIFEST.MF` — файл, в котором указан `Main-Class` (точка входа). Когда ты запускаешь `java -jar MathHelper.jar`, Java читает этот файл и знает, какой класс запускать.

В `pom.xml` мы исключаем `META-INF/*.SF`, `*.DSA`, `*.RSA` — это файлы цифровых подписей из оригинальных библиотек. Они не нужны в самодельном fat JAR и могут вызвать ошибки безопасности.

`module-info.class` мы тоже исключаем — это файлы модульной системы Java. Если они остаются в fat JAR, Java думает, что JavaFX лежит на classpath, а должен быть на module path, и выдаёт ошибку "JavaFX runtime components are missing".

### app.data — что это и зачем

`app.data` — это системное свойство Java, которое мы задаём в `MainApp.main()`:

```java
String appData = System.getProperty("user.home") + "/.uravnyashka";
System.setProperty("app.data", appData);
```

А в `logback.xml` мы пишем:
```xml
<file>${app.data}/logs/uravnyashka.log</file>
```

Это нужно, чтобы **логи писались в домашнюю папку пользователя**, а не в папку с программой. Если программа установлена в `C:\Program Files\MathHelper`, у пользователя нет прав на запись туда. Без этого фикса logback пытается создать файл в папке с программой — получает ошибку доступа и программа падает.

То же самое для истории — файл `history.json` сохраняется в `~/.uravnyashka/`.

### Полная схема проверки (команды PowerShell)

```powershell
# 1. Очистить и пересобрать
mvn clean package -DskipTests

# 2. Проверить main class в манифесте
jar xf target/MathHelper.jar META-INF/MANIFEST.MF
Get-Content META-INF/MANIFEST.MF
# Должен быть: Main-Class: ru.math.Launcher

# 3. Проверить, что Launcher есть внутри JAR
jar tf target/MathHelper.jar | Select-String "Launcher"
# Должен показать: ru/math/Launcher.class

# 4. Запустить JAR (должно открыться окно)
java -jar target/MathHelper.jar

# 5. Подготовить папку для jpackage
Remove-Item -Recurse -Force target/jpackage-input -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path target/jpackage-input -Force
Copy-Item target/MathHelper.jar target/jpackage-input/

# 6. Собрать MSI-установщик
$env:Path = "C:\Program Files (x86)\WiX Toolset v3.11\bin;" + $env:Path
jpackage --name MathHelper --input target/jpackage-input --main-jar MathHelper.jar `
  --main-class ru.math.Launcher --type msi --dest target --app-version 2.0.0 `
  --vendor "MathHelper" --win-menu --win-shortcut --win-dir-chooser `
  --icon src/main/resources/ru/math/images/icon.ico `
  --runtime-image "$env:JAVA_HOME"

# 7. Собрать портативную версию
jpackage --name MathHelper --input target/jpackage-input --main-jar MathHelper.jar `
  --main-class ru.math.Launcher --type app-image --dest target --app-version 2.0.0 `
  --vendor "MathHelper" --icon src/main/resources/ru/math/images/icon.ico `
  --runtime-image "$env:JAVA_HOME"

# 8. Проверить, что exe запускается
Start-Process target/MathHelper/MathHelper.exe
Start-Sleep -Seconds 5
Get-Process -Name "MathHelper" -ErrorAction SilentlyContinue
```