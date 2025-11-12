# 🌦️ WeatherApp
O aplicație client-server pentru consultarea prognozei meteo, împărțită în două module Java (Client și Server) care comunică prin socket-uri și folosesc o bază de date pentru stocarea datelor despre utilizatori și vreme.

## 📋 Descriere
WeatherApp este construită pe Java 21 și Maven, cu două proiecte independente:
- **Serverul** expune funcționalitățile de autentificare, administrare a locațiilor și furnizare a datelor meteo. Layer-ul de persistență folosește JPA (EclipseLink) peste PostgreSQL, cu DAO-uri dedicate pentru utilizatori, locații și temperaturi.
- **Clientul** este o aplicație de consolă ce orchestrează experiența utilizatorilor prin meniuri interactive. Mesajele sunt serializate în JSON (Gson) și trimise către server.

Fluxul tipic: utilizatorii își creează un cont local, introduc o locație (nume, latitudine, longitudine) și solicită prognoza pentru următoarele trei zile. Administratorii pot încărca rapid date suplimentare din fișierul JSON furnizat de server.

## 🚀 Caracteristici principale
- **Autentificare și înregistrare**: utilizatorii pot crea conturi sau se pot autentifica prin meniul clientului; primul cont creat primește automat rolul de administrator.
- **Roluri și permisiuni**: rolurile `ADMIN` și `USER` sunt gestionate în mod persistent; doar administratorii pot iniția operația de provisioning.
- **Selectarea locației**: utilizatorul configurează locația curentă direct din client, introducând „nume,latitudine,longitudine”; locațiile noi sunt salvate automat în baza de date.
- **Prognoză pe 3 zile**: serverul returnează temperatura actuală, descrierea vremii și temperaturile pentru următoarele trei zile. Dacă locația exactă lipsește, se caută cea mai apropiată locație înregistrată (raza implicită: 50 km).
- **Provisioning de date**: administratorii pot popula baza de date cu intrări noi din fișierul `weather_data.json`, mapat pe entități JPA existente.
- **Persistență abstractizată**: DAO-urile (UserDao, LocationDao, WeatherDao) encapsulează tranzacțiile JPA, iar clasa `Connection` simplifică inițializarea `EntityManager`-ului.

## 🧭 Fluxuri principale în aplicație
### Experiența utilizatorilor
2. **Autentificare locală**: meniul inițial permite autentificarea sau înregistrarea (comenzi 1 și 2). Datele sunt transmise sub forma `Request` JSON către server.
3. **Configurarea locației**: opțiunea „Set Location” solicită introducerea formatului `Nume,Latitudine,Longitudine` și persistă locația în baza de date dacă nu există deja.
4. **Consultarea vremii**: comanda „Fetch Weather Data” returnează prognoza formatată cu temperaturi și descrieri. Pentru locațiile necunoscute, serverul întoarce cea mai apropiată înregistrare disponibilă.
5. **Ieșire**: comanda „Exit” închide conexiunea socket și aplicația client.

### Administrarea datelor meteo
- **Provisionare**: administratorii pot alege opțiunea „Provision Data” pentru a popula baza de date cu intrările din `Server/src/main/resources/weather_data.json`.
- **Calcul locații apropiate**: `WeatherService` calculează distanța Euclidiană (aprox. kilometri) pentru a identifica locația cea mai apropiată în lipsa unei potriviri exacte.
- **Extensibilitate**: structura entităților (`LocationEntity`, `WeatherEntity`) permite adăugarea de noi câmpuri (precipitații, vânt etc.) fără modificări majore în client.

