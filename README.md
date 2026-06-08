# Mario Multiplayer

Prosta gra platformowa typu Mario z trybem wieloosobowym, wykorzystująca architekturę klient-serwer. 
Projekt korzysta z narzędzia **Gradle** do zarządzania zależnościami oraz z biblioteki **JavaFX** do wyświetlania grafiki.

## Wymagania
- Zainstalowane środowisko **Java (JDK)** w odpowiedniej wersji (np. Java 22, która jest zdefiniowana w konfiguracji).

## Jak uruchomić projekt?

Zarówno klient, jak i serwer uruchamiane są przy pomocy narzędzia Gradle, co gwarantuje automatyczne pobranie niezbędnych bibliotek (takich jak JavaFX) przed uruchomieniem aplikacji.

### 1. Uruchamianie Serwera
Aby gracze mogli połączyć się ze sobą, najpierw musi zostać uruchomiony serwer. Nasłuchuje on połączeń na domyślnym porcie `1234`.

1. Otwórz wiersz poleceń (terminal).
2. Przejdź do podfolderu `MarioServer`.
3. Uruchom serwer przy użyciu pliku `gradlew` znajdującego się katalog wyżej:

```powershell
cd MarioServer
..\gradlew.bat run
```

### 2. Uruchamianie Klienta (Gry)
Gdy serwer już działa, możesz uruchomić jednego lub więcej klientów gry.

1. Otwórz wiersz poleceń w głównym folderze projektu (tam, gdzie znajduje się główny plik `build.gradle`).
2. Uruchom grę następującym poleceniem:

```powershell
.\gradlew.bat run
```

*Pierwsze uruchomienie może potrwać nieco dłużej, ponieważ Gradle musi pobrać pliki JavaFX z internetu. Po zakończeniu uruchomi się właściwe okno z grą.*

---
**Uwaga techniczna dotycząca pliku `run.bat`:**
W głównym katalogu znajduje się plik `run.bat`, jednak **nie zaleca się jego używania** w standardowych środowiskach. Nie potrafi on automatycznie pobrać i podpiąć bibliotek JavaFX niezbędnych do działania gry. Korzystanie z Gradle (`.\gradlew.bat run`) jest jedynym niezawodnym i w pełni zautomatyzowanym sposobem uruchamiania aplikacji.
