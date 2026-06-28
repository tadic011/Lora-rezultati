# Lora Score

Android aplikacija za praćenje rezultata u kartaskoj igri Lora.

## Funkcije

- 4 igraca
- 4 partije / kruga
- igre: Plus, Minus, Pop Herc, Dame, Herčevi, Lora, Igra po želji
- pozitivni, negativni i nula poeni
- automatski Total Score
- pobednik je igrac sa najmanjim ukupnim brojem poena
- automatsko cuvanje unosa
- istorija zavrsenih partija
- tamna tema

## Build preko GitHub Actions

1. Uploaduj **sadrzaj ovog foldera** u GitHub repozitorijum.
2. Otvori tab **Actions**.
3. Izaberi **Build Android APK**.
4. Klikni **Run workflow**.
5. Kada se workflow zavrsi, preuzmi artifact **Lora-Score-APK**.
6. U artifactu je fajl `app-debug.apk` koji mozes instalirati na Android telefon.

Napomena: Ako Android telefon trazi dozvolu za instalaciju iz nepoznatih izvora, dozvoli je samo za aplikaciju iz koje otvaras APK.
