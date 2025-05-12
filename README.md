
Vytvořeno v: IDEA - JAVA 17 Marven

Použité Knihovny/Frameworky: 
- Springboot
    - Core
    - Web
    - Thymeleaf
- Caffeine

Spuštení serveru v samotném IDE - Nejnovější IntelliJ IDEA. Nepodařilo se mi spustit jako samotné binary, už čas nebyl. Přes Marven samotný i přes Github Actions

Překonfigurace v Provider.java na jiné API.

Interakce se Serverem:

WEB:

http://localhost:8080/api/v1/odlety-web

REST API možnosti:

http://localhost:8080/api/v1/odlety

api/v1/filter?letiste=<letkod>&od=<yyyyMMddHHmm - od dané doby>&do=<yyyyMMddHHmm po dobu>

Např:
- http://localhost:8080/api/v1/filter?letiste=EDHL&od=202405021100&do=202605071100
