# Robot Süpürge Simülasyonu - Uygulama Raporu

## Genel Mimari

Proje Java 17 ve JavaFX ile Maven projesi olarak kuruldu. Kod `com.robotvacuum` base package altında MVC mantığına göre ayrıldı.

- `model`: JavaFX bilmeyen saf domain sınıfları.
- `service`: algoritma ve hesaplama sınıfları.
- `controller`: kullanıcı eylemlerini ve simülasyon akışını yöneten sınıflar.
- `view`: JavaFX arayüzünü çizen sınıflar.
- `config`: sabit değerler ve demo oda kurulumu.

Bu ayrımın amacı, robotun kurallarını ekrandan bağımsız tutmaktır. Örneğin BFS algoritması `view` içinde değil, `service` içinde durur; böylece UI değişse bile algoritma bozulmaz.

## Model Katmanı

`Position`, grid içindeki `(row, col)` konumunu temsil eden value object olarak yazıldı. Hareket için `move(Direction)` metodu vardır.

`Cell`, her grid hücresinin kir, mobilya ve şarj istasyonu bilgisini tutar. Geçilebilirlik kontrolü burada yapılır.

`Room`, 12x20 `Cell[][]` grid yapısını yönetir. Kir ekleme, mobilya ekleme, sınır kontrolü ve aktif kir listesini üretme görevleri buradadır.

`Robot`, robotun konumunu, yönünü, bataryasını, durumunu, seçili temizlik algoritmasını ve hareket yollarını tutar.

`Battery`, batarya seviyesini 0-100 aralığında korur. Tüketim ve şarj işlemlerini kapsüller.

`Dirt`, kir türünü ve kalan temizlik süresini tutar. Toz, sıvı ve leke süreleri `DirtType` üzerinden gelir.

`Obstacle`, bir veya daha çok hücre kaplayan mobilyayı temsil eder.

`ChargingStation`, şarj istasyonunun konumunu tutar.

## Enumlar

`Direction`, robotun yönlerini ve sağa/sola/geri dönüş hesaplarını içerir.

`DirtType`, PDF'teki üç kir türünü temsil eder: Toz, Sıvı, Leke. Her türün temizlik süresi ve batarya maliyeti vardır.

`CleaningAlgorithm`, normal temizlik hareket seçeneklerini tutar: Rastgele, Spiral, Duvar Takip. BFS burada yer almaz; BFS yalnızca şarj istasyonuna dönüş içindir.

`SimulationState`, simülasyon durumlarını tutar: READY, RUNNING, PAUSED, CLEANING, RETURNING_TO_CHARGER, CHARGING, ERROR.

`CellType`, hücrenin genel mantıksal/görsel tipini temsil eder.

## Service Katmanı

`BfsPathfindingService`, robotun şarj istasyonuna en kısa yoldan dönmesi için BFS uygular. `Queue`, `Set` ve `Map` kullanır.

`CleaningMovementService`, robotun normal temizlik hareketini seçili algoritmaya göre belirler:

- Rastgele: geçerli komşu hücrelerden birini seçer.
- Spiral: 1,1,2,2,3,3 şeklinde genişleyen rota izlemeye çalışır.
- Duvar Takip: sağ-el kuralına göre sağ, ileri, sol, geri önceliğiyle hareket eder.

`StatisticsService`, toplam alan, engel alanı, kalan kir ve temizlenmiş alan yüzdesini hesaplar.

## Controller Katmanı

`SimulationController`, projenin ana akış yöneticisidir. Start, pause, reset, tick, batarya güncelleme, kir/mobilya ekleme, temizlik ve şarj dönüşü burada yönetilir.

`GridController`, grid tıklamalarını yorumlar. Kullanıcının seçtiği moda göre tıklamayı kir ekleme veya mobilya ekleme olarak `SimulationController`'a iletir.

## View Katmanı

`MainView`, ekranın ana yerleşimini kurar: sol kontrol paneli, orta grid ve alt istatistik paneli.

`ControlPanelView`, PDF görselindeki sol paneli oluşturur. Kir ekleme, mobilya ekleme, kir türü seçimi, hız slider'ı, temizlik algoritması, batarya girişi ve kontrol butonları buradadır.

`GridView`, oda görünümünü JavaFX Canvas ile çizer. Ahşap zemin hissi, koordinatlar, mobilyalar, kirler, robot, şarj istasyonu, mavi normal hareket izi ve turkuaz BFS dönüş yolu burada çizilir.

`StatusPanelView`, alt istatistik kartlarını gösterir: toplam alan, temizlenen alan, kalan alan, geçen süre ve toplam toz.

## Çalıştırma

Projeyi çalıştırmak için:

```bash
mvn javafx:run
```

Derleme kontrolü için:

```bash
mvn compile
```

## Doğrulama Durumu

`mvn compile` başarıyla çalıştı. JavaFX runtime komutu da başlatıldı ve açılış sırasında hata üretmedi.
