# PRD: JavaFX Robot Süpürge Simülasyonu

## 1. Project Overview
Java + JavaFX ile geliştirilecek masaüstü uygulama, 12x20 hücreli bir oda içinde robot süpürgenin hareketini, kir temizliğini, batarya yönetimini ve şarj istasyonuna BFS ile dönüşünü simüle eder.

UI, PDF’teki referans görsel hissine yakın olacak: sol kontrol paneli, merkez oda/grid alanı ve alt istatistik paneli bulunacak. Oda alanı yalnızca boş karelerden oluşmayacak; mobilya, zemin hissi, robot, şarj istasyonu, kirler ve hareket iziyle zenginleştirilecek.

PDF Precedence Rule:
- `/Users/bernakarli/Desktop/BZ_214_Visual_Programming_Assignment.pdf` bu proje için en üst gereksinim kaynağıdır.
- PRD, plan veya uygulama kararı PDF ile çelişirse PDF geçerli kabul edilecektir.
- PDF’teki yazılı gereksinimler ve ilk sayfadaki referans UI görseli uygulama boyunca dikkate alınacaktır.

## 2. Goals and Non-Goals
Goals:
- PDF’teki tüm zorunlu gereksinimleri karşılamak.
- JavaFX ile modern ve anlaşılır bir simülasyon arayüzü oluşturmak.
- MVC mimarisini uygulamak.
- Robot hareketi, kir temizliği, batarya tüketimi ve şarj dönüşünü görsel olarak göstermek.
- Gerçek zamanlı istatistikleri kullanıcıya sunmak.

Non-Goals:
- A* ilk sürümde uygulanmayacak.
- Bonus özellikler ilk sürümde uygulanmayacak.
- Üçüncü parti kütüphane kullanılmayacak.
- Gerçek fizik motoru veya ileri seviye yapay zeka hedeflenmeyecek.

## 3. Functional Requirements
- Kullanıcı simülasyonu başlatabilir, duraklatabilir ve sıfırlayabilir.
- Kullanıcı robotu şarj istasyonuna gönderebilir.
- Kullanıcı robot bataryasını manuel olarak 0-100 arası güncelleyebilir.
- Kullanıcı kir ekleyebilir ve kir türünü seçebilir.
- Kullanıcı engel ekleyebilir.
- Kullanıcı robot hızını değiştirebilir.
- UI’da Cleaning Algorithm seçim alanı bulunur.
- Cleaning Algorithm alanı robotun normal oda temizleme/hareket davranışını seçmek için kullanılacaktır.
- İlk sürümde Cleaning Algorithm seçenekleri `Rastgele`, `Spiral` ve `Duvar Takip` olacaktır.
- BFS, Cleaning Algorithm seçeneği değildir; yalnızca şarj istasyonuna en kısa dönüş için kullanılan pathfinding algoritmasıdır.
- A* ilk sürümde uygulanmayacak; future extension olarak kalacaktır.
- Sistem Dust, Liquid ve Stain kir tiplerini destekler.
- Robot duvarlardan ve engellerden geçemez.
- Robot kirli hücreden geçerken kiri temizler.
- Robot hareket ederken batarya tüketir.
- Robot kir temizlerken kir türüne göre ek batarya tüketir.
- Robot düşük bataryada BFS ile şarj istasyonuna döner.
- Konum, yön, batarya, temizlenen alan yüzdesi, kalan kirli alan ve geçen süre gerçek zamanlı güncellenir.

## 4. User Stories
- Kullanıcı olarak robotun oda içinde hareketini görmek istiyorum.
- Kullanıcı olarak grid’e Dust, Liquid veya Stain eklemek istiyorum.
- Kullanıcı olarak mobilya/engel eklemek istiyorum.
- Kullanıcı olarak simülasyonu başlatmak, duraklatmak ve sıfırlamak istiyorum.
- Kullanıcı olarak robotu istediğim anda şarja göndermek istiyorum.
- Kullanıcı olarak bataryayı manuel güncelleyebilmek istiyorum.
- Kullanıcı olarak robotun durumunu gerçek zamanlı takip etmek istiyorum.

## 5. Acceptance Criteria
- Uygulama JavaFX penceresi olarak açılır.
- Başlangıçta 12x20 demo oda görünür.
- Robot, şarj istasyonu, mobilyalar, kirler ve hareket izi görsel olarak ayrışır.
- Start sonrası robot deterministik yörüngede hareket eder.
- Robot engel veya duvara giremez; çarpınca yön değiştirir.
- Kirli hücreye gelince kir türüne göre bekleyerek temizler.
- Batarya her harekette ve temizlikte azalır.
- Batarya 20 veya altına inerse robot BFS ile şarja döner.
- Return to Charging Station butonu BFS dönüşünü başlatır.
- BFS yol bulamazsa simülasyon duraklar ve kullanıcıya uyarı gösterilir.
- Cleaning Algorithm alanında `Rastgele`, `Spiral` ve `Duvar Takip` seçenekleri görünür.
- Seçilen Cleaning Algorithm robotun normal temizlik hareketini değiştirir.
- BFS sadece düşük batarya veya Return to Charging Station durumunda şarj istasyonuna dönüş için kullanılır.
- Reset, demo başlangıç düzenine döndürür.
- Gerçek zamanlı istatistikler her tick güncellenir.

## 6. Business Rules
- Proje Java ile yazılacak.
- GUI JavaFX olacak.
- MVC zorunlu.
- Object-Oriented Programming best practice uygulanacak.
- Sadece Java SE ve JavaFX kullanılacak.
- Asset dosyaları `resources/assets` klasöründe tutulacak.
- Kullanılan tüm asset dosyaları teslim ZIP’ine dahil edilecek.
- Teslim ZIP’i kaynak kod, JavaFX proje dosyaları, asset’ler, README/kullanım kılavuzu, proje raporu ve ekran görüntüleri içerecek.

## 7. Simulation Rules
- Simülasyon tick tabanlı çalışır.
- Robot her tick’te ya hareket eder, ya temizlik yapar, ya şarja döner, ya şarj olur.
- Robot aynı anda tek hücrede bulunur.
- Robot temizleme sırasında hareket etmez.
- RUNNING sırasında kullanıcı sadece kir ekleyebilir; engel ekleme kapalıdır.
- Engel ekleme READY veya PAUSED durumlarında yapılabilir.

## 8. UI/UX Requirements
- Ana pencere PDF referans görselindeki gibi koyu mavi/füme temalı olacak.
- Üst başlık alanında `Robot Süpürge Simülasyonu` yazısı bulunacak.
- Sol panel PDF referans görselindeki akışa uygun olacak: Araçlar, Kir Türü, Robot Hızı, Temizlik Algoritması, Robot Durumu ve Kontroller.
- Araçlar bölümünde `Kir Ekle` ve `Mobilya Ekle` kontrolleri bulunacak.
- Kir Türü bölümünde `Toz`, `Sıvı` ve `Leke` seçenekleri bulunacak.
- Robot Hızı bölümünde slider ve anlık hız değeri bulunacak.
- Cleaning Algorithm alanı bir seçim alanı olarak tasarlanacak.
- Temizlik Algoritması seçenekleri `Rastgele`, `Spiral` ve `Duvar Takip` olacak.
- `Spiral` varsayılan seçili temizlik algoritması olacak; PDF görselindeki seçili seçenekle uyumludur.
- BFS, Temizlik Algoritması alanında gösterilmeyecek; şarja dönüş pathfinding algoritması olarak ayrı kalacak.
- Merkezde ahşap zemin hissi veren grid/oda alanı olacak.
- Merkez grid alanında üstte sütun, solda satır koordinatları bulunacak.
- Alt panelde PDF referans görselindeki gibi istatistik kartları bulunacak.
- Alt istatistik kartları en az `Toplam Alan`, `Temizlenen Alan`, `Kalan Alan`, `Geçen Süre` ve `Toplam Toz` bilgilerini gösterecek.
- Oda sadece boş karelerden oluşmayacak; mobilya ve çevre öğeleriyle zenginleştirilecek.
- Robot basit daire değil, üstten görünen robot süpürge gibi tasarlanacak.
- Şarj istasyonu dock görünümünde olacak.
- Normal hareket izi mavi kesikli çizgi, şarj dönüş yolu yeşil/turkuaz çizgi olacak.
- Temizlik animasyonu PDF’te bonus özellik olarak geçtiği için ilk sürümde zorunlu değildir; ileride eklenirse kir üzerinde solma/progress etkisi kullanılabilir.

## 9. Grid and Environment Rules
- Grid boyutu sabit: 12 satır x 20 sütun.
- Koordinatlar `(row, col)` formatında tutulur; row 0-11, col 0-19.
- Varsayılan demo yerleşimi:
  - Şarj istasyonu: `(10,0)`
  - Robot başlangıcı: `(10,1)`
  - Koltuk engeli: `(1,5)-(2,8)`
  - Orta masa engeli: `(5,5)-(6,8)`
  - Tekli koltuk engeli: `(6,2)-(7,3)`
  - Yemek masası engeli: `(5,14)-(7,16)`
  - Dolap engeli: `(9,18)-(10,19)`
  - Kirler: Dust `(1,3)`, `(3,15)`, `(8,4)`; Liquid `(4,10)`, `(9,12)`; Stain `(2,18)`, `(7,11)`
- Varsayılan demo odasında şarj istasyonuna geçerli yol bulunmalıdır.

## 10. Robot Behaviour Specification
- Başlangıç yönü: sağ.
- Robotun normal temizlik hareketi seçili Cleaning Algorithm değerine göre belirlenir.
- `Rastgele`: Robot geçerli komşu hücrelerden rastgele bir yön seçerek ilerler; duvar veya mobilyadan geçmez.
- `Spiral`: Robot spiral benzeri bir rota izlemeye çalışır; adım dizisi 1,1,2,2,3,3 şeklinde genişler, engel veya duvarla karşılaşırsa geçerli alternatif yöne döner.
- `Duvar Takip`: Robot sağ-el kuralına benzer şekilde duvar ve mobilya kenarlarını takip eder; sağ taraf açıksa sağa döner, değilse ileri/sol/geri alternatiflerini dener.
- Hiç geçerli yön yoksa robot bulunduğu hücrede kalır ve uyarı durumuna geçebilir.
- Robot kendi yörüngesinde dolaşır; özel olarak en yakın kiri hedeflemez.
- Robot kirli hücreden geçerken temizleme sürecini başlatır.

## 11. Battery Management Rules
- Başlangıç bataryası: 100.
- Düşük batarya eşiği: 20.
- Her hareket maliyeti: 1 batarya.
- Dust ek temizlik maliyeti: 2.
- Liquid ek temizlik maliyeti: 4.
- Stain ek temizlik maliyeti: 6.
- Batarya 0 altına düşemez, 100 üstüne çıkamaz.
- Manuel batarya güncelleme her durumda yapılabilir; geçerli aralık 0-100’dür.
- Şarj istasyonunda batarya her tick +5 artar.
- Batarya 100 olduğunda robot tekrar RUNNING durumuna döner.

## 12. Dirt Types and Cleaning Rules
- Dust: 1 tick temizleme süresi, 2 ek batarya maliyeti.
- Liquid: 3 tick temizleme süresi, 4 ek batarya maliyeti.
- Stain: 5 tick temizleme süresi, 6 ek batarya maliyeti.
- Kir temizlenene kadar hücre dirty kabul edilir.
- Temizlik tamamlandığında hücre temizlenir ve istatistikler güncellenir.
- Aynı hücrede birden fazla kir bulunamaz.

## 13. Pathfinding Requirements
- Şarj istasyonuna dönüşte BFS kullanılacak.
- BFS, düşük batarya durumunda otomatik tetiklenir.
- BFS, Return to Charging Station butonuyla manuel tetiklenir.
- BFS normal oda temizleme algoritması değildir.
- Seçili Cleaning Algorithm, robotun normal temizlik yörüngesini belirler; BFS sadece şarj istasyonuna dönüşte devreye girer.
- BFS yalnızca geçilebilir hücreleri kullanır.
- Engel, duvar ve geçersiz hücreler BFS dışında kalır.
- BFS mümkün olan en kısa yolu üretmelidir.
- A* ilk sürümde uygulanmayacak.
- A* future extension olarak `PathfindingStrategy` yapısına sonradan eklenebilir.
- BFS yol bulamazsa robot engelleri yok saymaz; simülasyon PAUSED olur ve kullanıcı uyarılır.

## 14. State Management
Durumlar:
- READY: başlangıç/düzenleme durumu.
- RUNNING: robot hareket eder ve temizler.
- PAUSED: zamanlayıcı durur, düzenleme yapılabilir.
- CLEANING: robot kir temizler.
- RETURNING_TO_CHARGER: robot BFS yolunu izler.
- CHARGING: robot şarj istasyonunda batarya doldurur.
- ERROR: şarj yolu yok veya kritik geçersiz durum.

Geçişler:
- READY -> RUNNING: Start.
- RUNNING -> PAUSED: Pause.
- RUNNING -> RETURNING_TO_CHARGER: düşük batarya veya Return butonu.
- RETURNING_TO_CHARGER -> CHARGING: istasyona varınca.
- CHARGING -> RUNNING: batarya 100 olunca.
- Her durum -> READY: Reset.

## 15. MVC Architecture Requirements
- Model, JavaFX sınıflarına bağımlı olmayacak.
- View, simülasyon kararları vermeyecek.
- Controller, kullanıcı olaylarını alıp Model’i güncelleyecek.
- Simülasyon tick yönetimi Controller tarafında olacak.
- BFS gibi algoritmalar service katmanında tutulacak.
- Asset yükleme View veya UI yardımcı sınıfları içinde yapılacak; Model asset dosyalarını bilmeyecek.

## 16. Project Structure Standard
Bu bölüm takım çalışması için bağlayıcı standarttır. Proje üç kişi tarafından farklı bölümlere ayrılarak geliştirileceği için paket, sınıf ve enum isimleri proje boyunca korunacaktır. Ekip üyeleri bu isimleri değiştirmemeli, aynı kavram için alternatif isimler üretmemeli ve entegrasyon sırasında bu standarda göre ilerlemelidir.

Base Package:
- `com.robotvacuum`

Required Packages:
- `com.robotvacuum.model`
- `com.robotvacuum.model.enums`
- `com.robotvacuum.controller`
- `com.robotvacuum.service`
- `com.robotvacuum.view`
- `com.robotvacuum.config`

Required Classes:
- `Position`
- `Cell`
- `Room`
- `Robot`
- `Battery`
- `Dirt`
- `Obstacle`
- `ChargingStation`

Required Enums:
- `Direction`
- `DirtType`
- `SimulationState`
- `CellType`
- `CleaningAlgorithm`

Naming Rules:
- Required class ve enum isimleri birebir bu PRD’de yazıldığı gibi kullanılacaktır.
- Ek sınıf oluşturmak serbesttir, ancak required class ve enum isimleri değiştirilemez.
- Enum sınıfları `com.robotvacuum.model.enums` paketi altında tutulacaktır.
- Model sınıfları JavaFX’e bağımlı olmayacaktır.
- View sınıfları model isimlerini değiştirmeden kullanacaktır.

## 17. Class Responsibilities
- `Room`: grid, sınır kontrolü, hücre erişimi.
- `Cell`: konum, kir, engel, şarj istasyonu bilgisi.
- `Position`: row/col değer nesnesi.
- `Robot`: konum, yön, durum, batarya ve hareket bilgisi.
- `Battery`: seviye, tüketim, şarj, manuel güncelleme.
- `Dirt`: kir türü ve temizlik ilerlemesi.
- `DirtType`: Dust/Liquid/Stain süre ve maliyetleri.
- `CleaningAlgorithm`: Rastgele, Spiral ve Duvar Takip normal temizlik hareket algoritmalarını temsil eder.
- `Obstacle`: geçilemeyen mobilya hücreleri.
- `ChargingStation`: istasyon konumu.
- `Direction`: robotun hareket yönlerini temsil eder.
- `SimulationState`: READY, RUNNING, PAUSED, CLEANING, RETURNING_TO_CHARGER, CHARGING ve ERROR durumlarını temsil eder.
- `CellType`: hücrenin boş, engel, kirli, şarj istasyonu veya robotla ilişkili görsel/mantıksal tipini temsil eder.
- `SimulationController`: start/pause/reset/tick/state yönetimi.
- `GridController`: grid tıklama ve ekleme davranışları.
- `BfsPathfindingService`: en kısa dönüş yolu hesaplama.
- `MainView`, `GridView`, `ControlPanelView`, `StatusPanelView`: JavaFX arayüz bölümleri.

## 18. Package Structure
- `com.robotvacuum.model`: domain sınıfları.
- `com.robotvacuum.model.enums`: enum sınıfları.
- `com.robotvacuum.view`: JavaFX ekran bileşenleri.
- `com.robotvacuum.controller`: kullanıcı etkileşimi ve simülasyon akışı.
- `com.robotvacuum.service`: BFS, istatistik hesaplama, validasyon servisleri.
- `com.robotvacuum.config`: sabit grid, batarya, süre ve demo layout değerleri.
- `resources/assets`: robot, şarj istasyonu, mobilya, zemin, kir veya UI görselleri.

## 19. Data Structures
- `Cell[][]`: 12x20 grid.
- `List<Position>`: robot hareket izi.
- `Queue<Position>`: BFS kuyruğu.
- `Map<Position, Position>`: BFS path reconstruction.
- `Set<Position>`: BFS visited hücreleri.
- `EnumMap<DirtType, CleaningRule>`: süre ve batarya maliyetleri.
- `CleaningAlgorithm`: robotun normal temizlik hareket stratejisini seçmek için kullanılır.
- `List<Obstacle>` veya `Set<Position>`: engel konumları.
- `List<Dirt>`: aktif kirler.

## 20. Validation Rules
- Grid dışına kir/engel eklenemez.
- Robot hücresine engel eklenemez.
- Şarj istasyonu hücresine kir veya engel eklenemez.
- Engel olan hücreye kir eklenemez.
- Kir olan hücreye ikinci kir eklenemez.
- RUNNING sırasında engel eklenemez.
- Engel ekleme, robotun şarj istasyonuna tüm geçişini kapatıyorsa reddedilir ve uyarı verilir.
- Batarya girişi yalnızca 0-100 arası kabul edilir.
- Asset dosyaları eksikse uygulama çökmeden yedek JavaFX şekilleriyle görsel oluşturmalıdır.

## 21. Error Handling
- Geçersiz batarya değeri: kullanıcıya hata mesajı gösterilir, değer uygulanmaz.
- Geçersiz hücreye ekleme: kısa uyarı gösterilir, grid değişmez.
- BFS yol bulamazsa: simülasyon PAUSED olur, uyarı mesajı gösterilir.
- Robot sıkışırsa: durum panelinde uyarı gösterilir.
- Asset yüklenemezse: fallback görsel kullanılır.
- Reset tüm hata durumlarını temizler.

## 22. Configuration Assumptions
Kesinleşmiş varsayımlar:
- Grid: 12x20.
- Başlangıç bataryası: 100.
- Düşük batarya eşiği: 20.
- Şarj hızı: +5/tick.
- Hareket maliyeti: 1.
- Dust: 1 tick, +2 batarya.
- Liquid: 3 tick, +4 batarya.
- Stain: 5 tick, +6 batarya.
- İlk sürümde BFS tek aktif pathfinding algoritmasıdır.
- İlk sürümde normal temizlik algoritmaları `Rastgele`, `Spiral` ve `Duvar Takip` olarak sunulur.
- Varsayılan normal temizlik algoritması `Spiral` olacaktır.
- A* future extension olarak kalır.
- Reset demo başlangıç düzenine döner.
- Asset dosyaları `resources/assets` altında tutulur ve ZIP’e eklenir.

## 23. Testing Requirements
- Robot duvardan geçemez testi.
- Robot engelden geçemez testi.
- Çarpınca yön değiştirme testi.
- Dust/Liquid/Stain temizlik süresi testi.
- Batarya hareket ve temizlik maliyeti testi.
- Düşük bataryada BFS dönüş testi.
- Return butonu BFS dönüş testi.
- BFS yol yoksa uyarı ve pause testi.
- Cleaning Algorithm alanında `Rastgele`, `Spiral` ve `Duvar Takip` seçeneklerinin görünmesi testi.
- `Spiral` varsayılan seçili algoritma testi.
- Seçilen Cleaning Algorithm değerine göre robotun normal temizlik yörüngesinin değişmesi testi.
- BFS’in Cleaning Algorithm seçeneği olarak gösterilmemesi testi.
- Manuel batarya 0-100 validasyon testi.
- RUNNING sırasında kir ekleme açık, engel ekleme kapalı testi.
- Reset demo başlangıca dönüyor testi.
- Gerçek zamanlı istatistik güncelleme testi.
- Asset dosyaları yüklendiğinde görsellerin görünmesi testi.
- Asset eksik olduğunda uygulamanın çökmeden fallback görsel kullanması testi.

## 24. Future Extension Points
- A*, `PathfindingStrategy` arayüzüyle eklenebilir.
- Cleaning Algorithm seçim alanı ileride yeni normal temizlik stratejileriyle genişletilebilir.
- Ulaşılamayan alan tespiti BFS/graph analiziyle eklenebilir.
- Çoklu oda düzenleri `config` veya layout seçiciyle eklenebilir.
- Ses efektleri View katmanına eklenebilir.
- Daha gelişmiş temizlik animasyonları GridView içinde genişletilebilir.
- Farklı mobilya türleri `Obstacle` görsel varyantlarıyla eklenebilir.

## 25. Final Implementation Checklist
- JavaFX proje iskeleti hazır.
- MVC paketleri ayrılmış.
- Base package `com.robotvacuum` olarak ayarlanmış.
- Required package, class ve enum isimleri Project Structure Standard bölümüne uygun kullanılmış.
- `resources/assets` klasörü oluşturulmuş.
- Kullanılan asset dosyaları `resources/assets` içine eklenmiş.
- 12x20 demo oda oluşturulmuş.
- Robot, şarj, engel, kir model sınıfları tamamlanmış.
- Sol kontrol paneli tamamlanmış.
- Sol panelde `Kir Ekle`, `Toz`, `Sıvı`, `Leke`, `Mobilya Ekle`, hız slider’ı, Temizlik Algoritması, Robot Durumu ve Kontroller bölümleri yer almış.
- Cleaning Algorithm seçim alanında `Rastgele`, `Spiral` ve `Duvar Takip` gösterilmiş.
- Varsayılan Cleaning Algorithm `Spiral` olarak ayarlanmış.
- Merkez grid/oda görünümü tamamlanmış.
- Alt istatistik paneli tamamlanmış.
- Alt panelde `Toplam Alan`, `Temizlenen Alan`, `Kalan Alan`, `Geçen Süre` ve `Toplam Toz` bilgileri gösterilmiş.
- Start/Pause/Reset çalışıyor.
- Return to Charging Station çalışıyor.
- Manuel batarya güncelleme çalışıyor.
- Dust/Liquid/Stain ekleme çalışıyor.
- Engel ekleme ve validasyon çalışıyor.
- Deterministik robot hareketi çalışıyor.
- Temizlik süreleri ve batarya maliyetleri uygulanmış.
- BFS dönüş yolu uygulanmış.
- Yol yok uyarısı uygulanmış.
- Gerçek zamanlı istatistikler güncelleniyor.
- README hazırlanmış.
- Ekran görüntüleri hazırlanmış.
- Asset dosyaları ZIP’e dahil edilmiş.
- Teslim ZIP’i PDF checklist’ine uygun hazırlanmış.
