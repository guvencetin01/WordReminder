# WordReminder 3.4.0

## 3.4.0 yenilikleri
- A2 seviyesindeki 25 günlük kelimeye İngilizce örnek cümle ve Türkçe çevirisi eklendi.
- Örnek cümle/çeviri paketi 749 satıra ulaştı. Etkili katalogda 1.060 İngilizce örnek ve 749 Türkçe çeviri bulunur.
- SQLite şeması 7 olarak kalır; kullanıcı verileri korunur.

# WordReminder 3.3.0

## 3.3.0 yenilikleri
- Hatırlatma aralığı ayarlarda kayıtlı dakika değerine göre uygulanır; varsayılan değer 10 dakikadır. Aralık değiştirildiğinde yeni süre hemen başlar. “Tekrar et” de ayarlı aralığı kullanır; “5 dk sonra” düğmesi açıkça seçilen tek seferlik ertelemedir.
- Bir hatırlatma kartı açıkken ikinci kart açılmaz. Aralık kart açıkken dolarsa tek bir hatırlatma beklemeye alınır ve mevcut kart kapanınca gösterilir.
- Kart, gösterildikten 2 dakika sonra kullanıcı etkileşimi olmadıysa otomatik kapanır.
- Otomatik zaman aşımında kullanıcı etkileşimi yoksa kart gösterimi sunum/öğrenme istatistiklerine yazılmaz.
- Kullanıcı kartla etkileşirse gösterim kaydı bir kez yazılır; kart kapatılabilir veya zor kelime olarak işaretlenebilir.
- SQLite şeması 7 olarak kalır; kullanıcı verileri korunur.

# WordReminder 3.2.0

## 3.2.0 yenilikleri
- Windows başlangıç ayarlarının metinleri ayrı açıklama satırlarıyla görünür hale getirildi. Sistem tepsisinde başlatma seçeneği, otomatik başlatmaya bağlandı.
- Hatırlatma kartı beyaz yerine açık yeşil zemin, ince koyu yeşil kenarlık ve uyumlu yeşil tonlu metin/butonlarla yenilendi.
- “Zor kelimelere ekle / çıkar” düğmesi artık kartı kapatmadan işaretlemeyi değiştirir. Öğrendim, Tekrar et ve 5 dk sonra seçenekleri kartı kapatmaya devam eder.
- 121 yeni günlük kelime için İngilizce örnek cümle ve Türkçe çeviri eklendi. Toplam içerik paketi 724 kelime-cümle-çeviri girdisine ulaştı.
- Etkili katalogda 1.035 İngilizce örnek cümle ve 724 Türkçe çeviri bulunur; 311 mevcut örnek için çeviri beklenir.
- SQLite şeması 7 olarak kalır; migration yoktur. Kişisel kelimeler, tekrar geçmişi ve ayarlar korunur.

## 3.1.0 yenilikleri
- Hatırlatma için kelime seçimi, gösterim kaydı ve cevap kaydı SQLite işlemleri JavaFX arayüz iş parçacığından ayrıldı.
- Kelime tablosu ve istatistikler arka planda yüklenir; arama 250 ms gecikmeyle toplulaştırılır ve eski sonuçların yeni sonuçları ezmesi engellenir.
- İlk katalog hazırlığı uygulama penceresini kilitlemeden arka planda yapılır; normal hatırlatma sayacı katalog hazırlandıktan sonra başlatılır.
- Kart gösterim geçmişi artık yalnızca pencere gerçekten gösterildikten sonra kaydedilir.
- Günlük kayıtları veritabanı kuyruğu, sorgu, JavaFX kuyruğu ve popup gösterim sürelerini ayrı ayrı ölçer.
- Mevcut İngilizce örnek cümlesi bulunan 100 teknik terime Türkçe çeviri eklendi. Örnek sayısı 914, çeviri sayısı 603 oldu.
- SQLite şeması 7 olarak kalır; yeni migration yoktur. Kişisel kelimeler, tekrar geçmişi ve ayarlar korunur.
- Zamanlayıcı için tetikleme, erteleme ve kapatma testleri eklendi.

## 3.0.0 yenilikleri
- Daha önce İngilizce örneği bulunan 100 kelimeye bağlama uygun Türkçe çeviri eklendi.
- Örneği bulunmayan 100 A1–A2 günlük kelime için yeni İngilizce cümle ve Türkçe çeviri eklendi.
- Toplam örnek cümle sayısı 914'e, Türkçe çevirisi bulunan örnek sayısı 503'e ulaştı.
- Hatırlatma zamanlayıcısı düzeltildi: sonraki aralık kart kapandıktan değil, kart gösterildiği andan itibaren sayılır.
- Kart açıkken yeni bir kart açılmaz; zaman gelirse sonraki deneme bir aralık ertelenir. Sistem üzerinden pencere kapatılırsa uygulama bunu da algılar.
- Zamanlayıcı ve JavaFX arayüz kuyruğundaki belirgin gecikmeler için `%APPDATA%\WordReminder\wordreminder.log` dosyasına sınırlı boyutta tanılama kaydı eklenir.
- Veritabanı şeması 7 olarak kalır; kişisel kelimeler ve öğrenme geçmişi korunur.

## 2.9.0 yenilikleri
- Örneği bulunmayan 100 günlük İngilizce kelime için yeni İngilizce örnek cümle ve Türkçe çeviri eklendi.
- İçerik kaynağı toplam 303 elle hazırlanmış cümle/çeviri çiftine ulaştı.
- Katalogdaki toplam İngilizce örnek cümle sayısı 814 oldu; 303 cümlenin Türkçe çevirisi hazır.
- Yalnızca yerleşik kelimeler güncellenir; kişisel kelimeler ve öğrenme verileri korunur.
- Yeni veritabanı migration'ı gerekmez; şema 7 olarak kalır.

## 2.8.0 yenilikleri
- Önceki paketteki 103 içerik girdisi 203'e çıkarıldı.
- Mevcut İngilizce örnek cümlelerin 100 tanesine bağlama uygun Türkçe çeviri eklendi.
- İçerik yalnızca yerleşik kelimeleri günceller; kişisel kelimeler ve öğrenme verileri korunur.
- Yeni veritabanı migration'ı gerekmez; şema 7 olarak kalır.

## 2.3.0 yenilikleri
- Ana başlık ve kelime işlemleri çubukları dar pencerelerde taşmak yerine satır kaydırır.
- Ayarlar kartları iki sütunlu düzene alındı; açıklamalar ve Windows başlatma seçenekleri dar alanda satır kaydırır.
- Kelime tablosu dar pencerelerde ikincil sütunları otomatik gizler; pencere genişletilince geri getirir.
- Üst istatistiklerde tekrar zamanı gelen kelime sayısı da gösterilir.
- Minimum pencere boyutu 760×560 olarak ayarlandı. Veritabanı şeması ve öğrenme algoritması değişmedi.

## 2.1.0 yenilikleri
- Ayarlar ve öğrenme ilerlemesi sekmelerine dikey kaydırma eklendi; içerik pencere yüksekliğinden bağımsız erişilebilir.
- Ayarları kaydet düğmesi sekmenin üst kısmına taşındı.
- Yedekleme/doğrulama ve öğrenme kapsamı kontrolleri dar genişliklerde satır kaydırır.
- Ana pencerenin minimum boyutu 820×600 olarak düşürüldü; uzun içerik kaydırma alanında kalır.

## 2.0.0 yenilikleri
- Ayarlar sekmesinden daha önce alınmış `.db` yedeklerini doğrulama.
- SQLite `integrity_check`, desteklenen şema sürümü ve kelime/kişisel kelime/tekrar/ayar kayıt sayıları raporlanır.
- Doğrulama salt okunurdur; canlı veritabanı ve seçilen yedek değiştirilmez.

## 1.9.0 yenilikleri
- Ayarlar sekmesine tam SQLite veritabanı yedeği alma özelliği eklendi.
- Yedek, VACUUM INTO ile tutarlı tek dosya olarak oluşturulur; kelimeler, kişisel kelimeler, tekrar geçmişi ve ayarlar birlikte saklanır.
- Yedekleme canlı veritabanını değiştirmez; şema yükseltmesi veya öğrenme verisi silme yapılmaz.

## 1.8.0 yenilikleri
- Öğrenme ilerlemesi ekranına günlük öğrenme serisi eklendi.
- Seri, yerel takvim gününde tamamlanan gerçek tekrar kayıtlarından hesaplanır; bugün henüz tekrar yapılmadıysa dünkü seri gün boyunca korunur.
- Bir takvim günü atlanırsa seri sıfırlanır; erteleme ve pencereyi kapatma seri hesabına dahil edilmez.
- SQLite şeması değiştirilmedi; mevcut geçmiş ve ayarlar korunur.

## 1.7.0 yenilikleri
- Ayarlardan günlük tekrar hedefini 1–100 arasında belirleyebilirsiniz (varsayılan 10).
- Öğrenme ilerlemesi ekranı bugünkü tamamlanan tekrarları hedefe göre gösterir.
- Yalnızca Öğrendim ve Tekrar et kararları sayılır; erteleme veya pencereyi kapatma hedefi ilerletmez.
- Hedef ayarı mevcut SQLite ayar tablosunda tutulur; veritabanı şeması değişmez.

## 1.6.0 yenilikleri

- Hatırlatma penceresi artık aktif hatırlama yöntemiyle çalışır: önce İngilizce kelimeyi görür, anlamını kendiniz hatırlamaya çalışırsınız.
- "Anlamı göster" düğmesi Türkçe karşılığı açar; ardından "Öğrendim" veya "Tekrar et" seçebilirsiniz.
- Anlamı açmadan kapatma veya erteleme, öğrenme puanını ve tekrar geçmişini değiştirmez.
- Veritabanı şeması değişmediği için mevcut öğrenme geçmişi, kişisel kelimeler ve ayarlar korunur.

Windows için masaüstünde çalışan İngilizce kelime öğrenme ve hatırlatma uygulaması. Java 26, JavaFX 26 ve SQLite kullanır; Apache NetBeans 31 ile Maven projesi olarak açılabilir.

## Katalog özellikleri

- **4.654 kelimelik** katalog korunmuştur: 3.654 günlük İngilizce ve 1.000 yazılım/teknik İngilizce.
- Her kelimede kelime türü alanı bulunur. Bu etiketler sözlükbilimsel olarak kesin değil, öğrenmeye yardımcı yaklaşık sınıflandırmalardır.
- **4.582 kelime için ABD İngilizcesi IPA telaffuzu** eklenmiştir. Kaynakta karşılığı bulunmayan 72 kelimenin telaffuzu boş bırakılır.
- Örnek cümle sayısı **914** olmuştur: 454 günlük ve 460 teknik örnek. Bunların **603** tanesi Türkçe cümle çevirisine sahiptir; 311 örneğin çevirisi henüz hazırlanmamıştır. Ayrıca 3.740 katalog kelimesinde henüz örnek cümle bulunmaz. Teknik katalogdaki örnek cümleler yazılım geliştirme bağlamına göre hazırlanmıştır.
- Yazılım terimlerinin bazı Türkçe karşılıkları bağlamı netleştirecek şekilde düzenlenmiştir.
- Ana kelime tablosunda IPA ve kelime türü sütunları; hatırlatma penceresinde telaffuz/tür bilgisi gösterilir.
- 1.8.0 için veritabanı şema yükseltmesi gerekmez; mevcut öğrenme geçmişi, kişisel kelimeler ve ayarlar korunur. 1.2 öncesi eski veriler 1.2.0'ın temiz başlangıç kararı gereği zaten taşınmaz.
- Yerleşik katalog güncellenirken mevcut yerleşik kelimeler İngilizce başlık üzerinden güncellenir. Türkçe karşılığı değişen bir kelime ikinci bir yerleşik kayıt olarak çoğaltılmaz.

## Kelime sayıları

| Seviye | Günlük | Teknik | Toplam |
|---|---:|---:|---:|
| A1 | 725 | 25 | 750 |
| A2 | 725 | 106 | 831 |
| B1 | 729 | 196 | 925 |
| B2 | 725 | 266 | 991 |
| C1 | 725 | 261 | 986 |
| C2 | 25 | 146 | 171 |
| **Toplam** | **3.654** | **1.000** | **4.654** |

CEFR, dil yeterliliğini iletişimsel görevler üzerinden tanımlar; her kelime için değişmez/resmî seviye vermez. Bu projedeki seviyeler öğrenme sırası için yaklaşık sınıflandırmalardır. Özellikle teknik terimlerde seviye, kullanıldığı bağlama göre değişebilir.

## Telaffuz ve kelime türü

- IPA gösterimi CMU Pronouncing Dictionary'nin ABD İngilizcesi kayıtlarından dönüştürülmüştür.
- Telaffuz kaydı bulunamayan kelimelerde tahmin yürütülmez.
- Birleşik ifadelerde sözcük kayıtları birleştirilebilir; bu nedenle gösterim doğal konuşmadaki ifade vurgusunun eksiksiz fonetik çözümlemesi değildir.
- Kelime türü alanı otomatik/kurallı sınıflandırma ve sözlük işaretlerinden yararlanır; bir kelime birden fazla türde kullanılabilir. Gösterilen tür, öğrenmeye yardımcı bir özet olarak değerlendirilmelidir.

## Örnek cümleler

Katalogdaki 1.035 İngilizce örneğin 724 tanesi Türkçe çevirisiyle birlikte gösterilir. 2.7.0, 2.9.0 ve 3.0.0 sürümlerinde toplam 300 günlük kelime için yeni cümle/çeviri çiftleri elle hazırlanmıştır. Ayrıca daha önce örneği bulunan 200 kelimenin Türkçe cümle çevirisi sonradan eklenmiştir. Kalan 311 mevcut örneğin Türkçe çevirisi ve örneği bulunmayan 3.740 kelime için yeni içerik, doğruluk kontrolünden geçirilmiş küçük paketlerle aşamalı olarak hazırlanacaktır. Eksik içerik tahminî veya otomatik çeviriyle doldurulmaz.

## Özellikler

- Varsayılan 10 dakikalık hatırlatma aralığı; 1–180 dakika arasında ayarlanabilir. Süre, kartın ekranda gösterildiği anda başlar. Kart açık kalırsa üst üste pencere açılmaz; sonraki deneme bir tam aralık ertelenir.
- Zamanlayıcı ve JavaFX arayüz kuyruğu gecikmeleri `%APPDATA%\WordReminder\wordreminder.log` dosyasına yazılır (en fazla yaklaşık 1 MB etkin günlük ve bir döndürülmüş günlük).
- Bilgisayar uyku/hibernasyon sırasında uygulama hatırlatma penceresi gösteremez; zamanlayıcının devam edebilmesi için Windows'un uyanık olması gerekir.
- Sistem tepsisinde arka planda çalışma.
- Küçük özel kelime popup'ı.
- A1-C2 seviye ve Günlük/Teknik kategori filtreleri.
- Kelime arama.
- Kişisel kelime ekleme, düzenleme ve silme.
- SQLite üzerinde kelime, ayar ve öğrenme geçmişi.
- SM-2 esinli uyarlanabilir tekrar aralıkları; doğru yanıtta kişisel kolaylık katsayısı ile 365 güne kadar uzama, zorlanılan kelimede 10 dakika sonra tekrar.
- CEFR seviyelerine göre öğrenme ilerlemesi, öğrenilmiş kelime sayısı ve doğru yanıt oranı.
- Windows ile otomatik başlatma seçeneği.

## Gereksinimler

- Windows 10 veya Windows 11 (x64)
- JDK 26 (OpenJDK 26.0.2 uygundur)
- Apache Maven 3.9+
- İlk derlemede Maven Central erişimi

## NetBeans 31 ile çalıştırma

1. ZIP dosyasını bir klasöre çıkar.
2. NetBeans'te **File → Open Project** seçeneğiyle `WordReminder` klasörünü aç.
3. Proje Java Platform ayarının JDK 26 olduğundan emin ol.
4. Maven bağımlılıklarının indirilmesini bekle.
5. Projeyi çalıştır. Terminal alternatifi:

   ```bash
   mvn clean javafx:run
   ```

Derleme:

```bash
mvn clean package
```

## Windows kurulum paketi

`scripts/package-windows.bat` dosyasını Windows üzerinde çalıştır. `jpackage` JDK 26 ile gelir. `.exe`/`.msi` üretimi için WiX Toolset'in kurulu ve PATH üzerinde olması gerekir.

## Veri konumu ve güncelleme

Veritabanı `%APPDATA%\WordReminder\wordreminder.db` konumunda saklanır.

### Yedek alma ve geri yükleme
1. **Ayarlar → Veritabanını yedekle…** seçeneğini kullanarak `.db` yedeğini istediğin konuma kaydet.
2. Geri yüklemek için WordReminder'ı tamamen kapat.
3. Mevcut `%APPDATA%\WordReminder\wordreminder.db` dosyasını önce başka bir konuma kopyalayarak ayrıca koru.
4. Yedek `.db` dosyasını `wordreminder.db` adıyla bu klasöre kopyala ve uygulamayı yeniden başlat.

Yedekleme, SQLite `VACUUM INTO` ile tutarlı bir anlık görüntü üretir; uygulamanın açık veritabanını değiştirmez. Yedek dosyası mevcutsa yeni dosya başarıyla oluşturulduktan sonra değiştirilir. 1.7.0 günlük hedef ayarını mevcut `app_settings` tablosunda saklar; günlük seri mevcut `word_reviews` kayıtlarından hesaplanır. 1.9.0, SQLite `VACUUM INTO` kullanarak kullanıcıya seçtiği konumda tam veritabanı yedeği oluşturur; canlı veritabanı ve şema değiştirilmez. 2.0.0, dışa aktarılan yedekleri salt okunur SQLite bütünlük ve içerik sayımı kontrollerinden geçirir. 2.1.0, uzun sekme içeriklerini kaydırılabilir hâle getirir. 2.3.0, araç çubuklarını, ayar kartlarını ve kelime tablosu sütunlarını pencere genişliğine göre düzenler. 1.2 öncesi şemalar için eski temiz başlangıç davranışı korunur.

## Proje yapısı

```text
src/main/java/com/wordreminder/
├── MainApplication.java
├── data/Database.java
├── model/CefrLevel.java
├── model/WordCategory.java
├── model/Word.java
├── repository/WordRepository.java
├── repository/SettingsRepository.java
├── service/ReminderScheduler.java
├── service/WindowsStartupManager.java
└── ui/MainWindow.java
   ui/ReminderPopup.java

src/main/resources/com/wordreminder/data/word-catalog.tsv
src/main/resources/com/wordreminder/ui/app.css
```

## Sınırlamalar

- Hatırlatmalar uygulama açıkken çalışır. Bilgisayar kapalıyken veya kullanıcı oturumu kapalıyken bildirim gösterilemez.
- Hazır kelimeler doğrudan düzenlenemez; kişisel kelimeler düzenlenebilir ve silinebilir.
- Katalogdaki her kelime için örnek cümle yoktur.
- Seviye ve kelime türü etiketleri pedagojik/otomatik yaklaşık sınıflandırmalardır.
- IPA, ABD İngilizcesi için sağlanır; 72 kelimede telaffuz kaydı yoktur.
- Sesli telaffuz, bulut senkronizasyonu ve CSV içe/dışa aktarma bu sürümde yoktur.

## Veri kaynakları

- LexiCore-5000: A1-C1 İngilizce başlık kelimeleri, CC BY 4.0.
- FreeDict English-Turkish 0.3: Türkçe karşılıklar için kullanılan sözlük kaynağı; GPL-2.0-or-later lisans bildirimi.
- CMU Pronouncing Dictionary: ABD İngilizcesi telaffuz verisi; lisans metni `THIRD-PARTY-LICENSES/CMU-PRONOUNCING-DICTIONARY.txt` içinde.
- Yazılım/teknik alandaki yeni terimler, bağlama özel Türkçe karşılık düzenlemeleri ve örnek cümleler WordReminder için özgün olarak derlenmiştir.
- Ayrıntılı atıf ve lisans notları `LICENSES-DATA.md` içindedir.


## 2.3.0 – Öğrenme durumu filtreleri

Kelime havuzuna öğrenme durumuna göre filtre eklendi: henüz çalışılmayan kelimeler (`last_reviewed_at IS NULL`), tekrar zamanı gelen kelimeler (`next_due_at <= now`) ve öğrenilmiş kelimeler (`interval_days >= 21`). Filtre yalnızca mevcut alanları okur; SQLite şeması, tekrar geçmişi ve kullanıcı ayarları değişmez.


## 2.9.0 – Örnek cümle kapsamının genişletilmesi

- Daha önce örnek cümlesi bulunmayan 100 günlük kelimeye İngilizce cümle ve Türkçe çeviri eklendi.
- Yeni satırlar katalogla eşleştirildi; yinelenen başlık veya katalogda olmayan kelime bırakılmadı.
- Toplam cümle/çeviri kaynağı 303 girdiye, İngilizce örnek kapsamı 814 kelimeye ulaştı.
- SQLite şeması 7 olarak kaldı. Yalnızca yerleşik kelimelerin örnek alanları güncellenir; kişisel kelimeler ve öğrenme geçmişi korunur.

## 2.7.0 – Örnek cümleler ve kart tasarımı

- Hatırlatma kartı yeniden tasarlandı: kelime, seviye, telaffuz, örnek cümle ve çeviri daha belirgin bir hiyerarşiyle gösterilir. İçerik uzun olduğunda kart kaydırılabilir.
- İngilizce örnek cümlenin hemen altında Türkçe çevirisi gösterilir. Çeviri henüz hazırlanmadıysa bu durum açıkça belirtilir; otomatik ve doğrulanmamış çeviri üretilmez.
- Veritabanına `example_translation` alanı eklendi (şema 6 → 7). Mevcut kelimeler, tekrar geçmişi, zor kelime işaretleri, istatistikler ve ayarlar korunur.
- Kişisel kelime ekleme/düzenleme formuna İngilizce örnek ve Türkçe çeviri alanları eklendi.
- İlk içerik çalışması olarak 103 temel kelime için bağlama uygun İngilizce cümle ve Türkçe çeviri hazırlandı. Bu içerik yerleşik kelimeler için seed edilir; özel kelimeler etkilenmez.
- Örnek cümle kaynağı `src/main/resources/com/wordreminder/data/example-sentences.tsv` dosyasında tutulur. Yeni cümle eklerken İngilizce başlıkla eşleşen kelime kontrol edilmeli ve çeviri elle doğrulanmalıdır.
