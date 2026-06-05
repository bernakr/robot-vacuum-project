# Robot Süpürge Simülasyonu

Java 17 ve JavaFX ile hazırlanmış BZ 214 Robot Süpürge Simülasyonu projesidir. Bu proje BZ 214 Görsel Programlama dersi kapsamında geliştirilmiştir. Ders sorumlusuna ve katkıda bulunanlara özel teşekkürler.

## Çalıştırma

```bash
mvn javafx:run
```

## Özellikler

- 12x20 oda grid'i
- Robot, şarj istasyonu, mobilyalar, kirler ve hareket izi
- Kir türleri: Toz, Sıvı, Leke
- Normal temizlik algoritmaları: Rastgele, Spiral, Duvar Takip
- Düşük bataryada veya kullanıcı isteğinde BFS ile şarj istasyonuna dönüş
- Start, Pause, Reset, İstasyona Dön kontrolleri
- Manuel batarya güncelleme
- Gerçek zamanlı konum, yön, batarya, süre ve temizlik istatistikleri

## Mimari

Proje MVC yaklaşımıyla ayrılmıştır:

- `model`: domain sınıfları ve enumlar
- `service`: BFS, hareket algoritmaları ve istatistik hesaplama
- `controller`: simülasyon akışı ve kullanıcı etkileşimleri
- `view`: JavaFX arayüz bileşenleri
- `config`: sabitler ve demo oda kurulumu

config      → hazır oda / başlangıç ayarları
controller  → butonlar ve ekran olaylarını yönetir
model       → robot, oda, hücre, kir, mobilya gibi veriler
service     → temizlik algoritması, yol bulma, istatistik
view        → JavaFX ekranları, grid çizimi, panel tasarımı
resources   → css, görseller