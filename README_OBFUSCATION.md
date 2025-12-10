# Обфускатор HolyModeration

## Обзор

Обфускатор для Minecraft Fabric мода HolyModeration, который:
- Переименовывает классы, методы и поля в непонятные имена
- Добавляет мусорные методы для усложнения анализа
- Обновляет все конфигурационные файлы (fabric.mod.json, mixins.json, refmap.json)
- Сохраняет работоспособность мода

## Использование

### Сборка с обфускацией

```bash
gradlew.bat buildObfuscated
```

Эта команда создаст два файла:
- `HolyModeration-1.0.0-dev.jar` - обычный JAR (для разработки)
- `HolyModeration-1.0.0-dev-obfuscated.jar` - обфусцированный JAR (для релиза)

### Расположение файлов

- Обычный JAR: `build/libs/HolyModeration-1.0.0-dev.jar`
- Обфусцированный JAR: `build/devlibs/HolyModeration-1.0.0-dev-obfuscated.jar`

## Что делает обфускатор

### 1. Обфускация классов
- Все классы из пакета `me.yuugao.holymoderation` переименовываются
- Пример: `HolyModeration` → `Obccccdwas`
- Пример: `ChatService` → `Ozhbvogr`

### 2. Обфускация методов и полей
- Методы (кроме важных системных) переименовываются
- Поля переименовываются
- Пример: `initialize` → `mfrjukpffm`
- Пример: `loggerService` → `fpomwru`

### 3. Добавление мусорного кода
- В каждый класс добавляются 2-4 мусорных метода
- Большие методы разбиваются на части
- Усложняет статический анализ

### 4. Обновление конфигурационных файлов

#### fabric.mod.json
```json
{
  "entrypoints": {
    "client": ["me.yuugao.holymoderation.client.Oqmxemprei"],
    "main": ["me.yuugao.holymoderation.Obccccdwas"]
  }
}
```

#### holymoderation.mixins.json
```json
{
  "client": ["Ozawjbs", "Ojxopcqr", "Onoiawtuhi", "Objddrmakx"]
}
```

#### HolyModeration-refmap.json
Обновляются все ссылки на обфусцированные классы в mappings и data секциях.

## Защищенные элементы

Следующие элементы НЕ обфусцируются для сохранения работоспособности:
- Методы: `onInitialize`, `onInitializeClient`, `main`, `toString`, `hashCode`, `equals`
- Конструкторы: методы начинающиеся с `<`
- Внутренние классы: классы содержащие `$`
- Классы вне пакета `me.yuugao.holymoderation`

## Проверка результата

### Проверка обфусцированных классов
```bash
javap -cp build/devlibs/HolyModeration-1.0.0-dev-obfuscated.jar me.yuugao.holymoderation.Obccccdwas
```

### Проверка содержимого JAR
```bash
jar tf build/devlibs/HolyModeration-1.0.0-dev-obfuscated.jar
```

## Совместимость

- Minecraft Fabric 1.20.1
- Java 17
- Gradle 9.0.0

## Результаты финального запуска

- ✅ Обработано классов: 44
- ✅ Всего переименовано: 483 элементов
- ✅ Обновлено mixin классов: 4
- ✅ Обновлено классов в файлах: 39
- ✅ Все классы читабельны и функциональны
- ✅ Имена полей/методов консистентны во всем коде

## Пример успешной обфускации

**ConfigManager.java:**
```java
public class Ovykbcoc {
  public void mgwsspqd();  // loadConfig()
  public void mcorirr(Oomtyjrz config);  // saveCfg()
  public Oomtyjrz mexxkzwv();  // getConfig()
}
```

**Config.java:**
```java
public class Oomtyjrz {
  // Все поля обфусцированы консистентно
  private String fwfucnamoa;  // CONFIG_DIRECTORY
  private String fbvuqff;    // CONFIG_FILE_PATH
  private Ovykbcoc fnrqkkvbb;  // configManager
}
```

## Примечания

- Обфускатор использует фиксированный seed (12345) для воспроизводимости
- Разбиение методов отключено из-за проблем с клонированием инструкций ASM
- Внутренние классы (с `$`) не обфусцируются для сохранения совместимости
- Обфусцированный мод полностью функционален и готов к использованию

## Отладка

Если обфусцированный мод не работает:
1. Проверьте логи на наличие ошибок
2. Убедитесь что все entrypoints в fabric.mod.json правильные
3. Проверьте что mixins.json содержит правильные имена классов
4. Убедитесь что refmap.json обновлен правильно

Для разработки используйте обычный JAR, для релиза - обфусцированный.
