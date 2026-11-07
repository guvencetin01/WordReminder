## 3.0.0 zamanlama incelemesi ve içerik notları
- `ReminderScheduler` önceki sürümde her hatırlatma aralığını kart kullanıcı tarafından kapatıldıktan/işlendikten sonra başlatıyordu. Kullanıcının kartta kaldığı süre her döngüde birikerek görüntüleme sıklığını ayarlanan değerin üzerine çıkarıyordu.
- Yeni davranış: başlangıçta ilk kart ayarlı süre sonunda gösterilir; kart gösterildiğinde bir sonraki düzenli aralık başlar. Kart zamanlayıcı tetiklendiğinde hâlâ açıksa ikinci kart açılmaz ve bir sonraki deneme bir tam aralık sonrasına alınır.
- `KNOWN`, `HARD`, `UNHARD` ve `DISMISS` işlemleri var olan düzenli zamanlayıcıyı gereksiz yere yeniden başlatmaz. `REPEAT` 10 dakika, `SNOOZE` 5 dakika için bekleyen zamanı geçici olarak değiştirir.
- `ReminderPopup` `setOnHidden` ile sistem/klavye kaynaklı kapatmayı `DISMISS` olarak işler. `finished` bayrağı callback'in bir defa çalışmasını sağlar; uygulama kapanırken `close()` sessizdir.
- Zamanlayıcı ve JavaFX kuyruğu 2 saniye veya daha fazla gecikirse `%APPDATA%\WordReminder\wordreminder.log` dosyasına kayıt yazılır. Günlük 1 MB seviyesinde döndürülür; kelime içerikleri veya kullanıcı girişleri yazılmaz.
- `MainApplication` sadece veri değiştiğinde `refreshData()` çağırır. `SNOOZE` ve `DISMISS` artık 4.654 satırlık tabloyu ve istatistik panelini boş yere JavaFX iş parçacığında yeniden yüklemez.
- `MainWindow.saveSettings()` yalnızca hatırlatma dakika değeri gerçekten değiştiyse `intervalChanged` çağırır; kategori, seviye veya Windows başlangıç ayarı kaydı mevcut bekleme süresini sıfırlamaz.
- Hatırlatma ayarı 1–180 aralığında okunup yazılır; eski/bozuk ayar değerleri çalışma anında güvenli aralığa sıkıştırılır.
- İçerik kaynağı 503 benzersiz satıra çıktı: 100 mevcut örneğe çeviri ve 100 yeni A1–A2 günlük örnek/çeviri çifti eklendi. Toplam etkili katalog: 914 İngilizce örnek, 503 Türkçe çeviri, 411 çevirisiz örnek, 3.740 örneksiz kelime.
- Veritabanı migration'ı yoktur; schema 7, kullanıcı kayıtları ve tekrar geçmişi aynen korunur.

# WordReminder geliştirme notları

## 1.4.0 kararları

- Yerleşik katalog UTF-8 TSV biçimindedir ve 7 alandan oluşur: `CEFR|CATEGORY|ENGLISH|TURKISH|PART_OF_SPEECH|US_IPA|EXAMPLE`.
- Katalog 4.654 benzersiz başlık içerir: 3.654 DAILY ve 1.000 TECHNICAL.
- 4.582 kelimede CMU Pronouncing Dictionary kaynaklı ABD IPA gösterimi vardır; kaynağı bulunmayan 72 telaffuz alanı boş kalır.
- 614 örnek cümle bulunur: 154 DAILY, 460 TECHNICAL.
- Kelime türü sınıflandırması yaklaşık/öğretim amaçlıdır; tek bir bağlamı veya tüm dilbilgisel kullanımları temsil ettiği iddia edilmez.
- CMUdict telaffuz verisinin lisansı `THIRD-PARTY-LICENSES/CMU-PRONOUNCING-DICTIONARY.txt` dosyasında tutulur.
- 1.2.0 şemasından 1.4.0 şemasına geçiş yalnızca iki metadata sütunu ekler; öğrenme geçmişi, sayaçlar ve ayarlar korunur.
- 1.2 öncesi veriler 1.2 temiz başlangıç sürümünde zaten silinmiştir; bu sürüm bu eski verileri geri getirmez.
- Katalog seed işlemi yerleşik kelimeyi İngilizce başlığa göre günceller; Türkçe karşılık revizyonu aynı başlıkta ikinci bir yerleşik kelime oluşturmaz.
- CEFR düzeyleri ve kelime türleri kesin/sertifikalı sınıflandırmalar değil, öğrenme desteği için yaklaşık etiketlerdir.

## Tekrar algoritması

- İlk uygulama hatırlatması ayarlanan süreden sonra gösterilir (varsayılan 10 dakika).
- "Tekrar et": öğrenme aralığını sıfırla ve 10 dakika sonra yeniden göster.
- "Öğrendim": SM-2 esinli kişisel aralık; ilk tekrar 1 gün, ikinci tekrar 3 gün, sonraki aralıklar kolaylık katsayısına göre en fazla 365 gün.
- "5 dk sonra": kelime inceleme kaydı oluşturmadan popup'ı ertele.


## 1.5.0 geliştirme notları

- Öğrenme ilerlemesi CEFR başına daha belirgin, sıfır değerinde de görünür ilerleme çubukları kullanır.
- İlerleme özetinde örnek cümlesi bulunan ve bulunmayan kelime sayıları birlikte gösterilir; eksik örnek kapsamı gizlenmez.
- Bu sürümde otomatik örnek cümlesi üretimi yapılmaz. Dilsel doğruluğu doğrulanmamış cümleler katalog verisine eklenmez.
- 1.5.0 yeni SQLite şeması gerektirmez; öğrenme geçmişi, kişisel kelimeler ve ayarlar değiştirilmeden korunur.


## 1.8.0 geliştirme notları
- Günlük tekrar hedefi `app_settings` içindeki `daily_goal` anahtarıyla saklanır; varsayılan 10, sınırlar 1–100.
- İlerleme sayacı `word_reviews.reviewed_at` üzerinden yerel takvim gününü esas alır. Yalnızca KNOWN/REPEAT kayıtları bulunur.
- SQLite şema sürümü değişmez; migration gerekmez.

## 1.6.0 geliştirme notları

- Hatırlatma penceresinde Türkçe karşılık başlangıçta gizlidir; "Anlamı göster" ile görünür olur. Bu, kelimeyi görür görmez cevabı okumak yerine aktif hatırlamayı teşvik eder.
- "Öğrendim" ve "Tekrar et" eylemleri yalnızca anlam açıldıktan sonra kullanılabilir. Pencereyi kapatma ve "5 dk sonra" erteleme inceleme kaydı oluşturmaz.
- Tekrar algoritması ve SQLite şeması değişmedi. Normal güncellemede mevcut kullanıcı verileri korunur.


## 1.9.0 geliştirme notları
- Kullanıcı, Ayarlar sekmesinden FileChooser ile `.db` yedek hedefi seçebilir.
- `Database.backupTo` SQLite `VACUUM INTO` ile tutarlı snapshot üretir.
- Yedek; words, word_reviews ve app_settings tablolarını içerir.
- Normal uygulama başlangıcında hiçbir veriyi sıfırlamaz; schema version 4 olarak kalır.


## 2.0.0 geliştirme notları
- Yedek doğrulama işlemi ayrı bağlantı üzerinden salt okunur sorgularla yapılır.
- SQLite integrity_check sonucu ve beklenen tabloların kayıt sayıları raporlanır.
- Canlı veritabanı ve yedek dosyası doğrulama sırasında değiştirilmez.
- Şema sürümü 4 olarak korunur; migration gerekmez.


## 2.1.0 geliştirme notları
- Ayarlar ve öğrenme ilerlemesi içerikleri ScrollPane içine alındı; içerik kırpılmaz.
- Ayarlar kaydetme düğmesi panelin üst satırında tutulur.
- FlowPane ile ayar filtreleri ve yedekleme butonları dar görünümde satır kaydırır.
- Veritabanı şeması ve kullanıcı verileri değişmez.


## 2.2.0 geliştirme notları
- Başlık ve kelime listesi araç çubuklarında FlowPane kullanılır; dar görünümde kontroller yeni satıra geçer.
- Ayarlar bölümü iki sütunlu GridPane kartlarına ayrılır. Tüm açıklamalar satır kaydırır ve ana ayar kaydetme eylemi üstte kalır.
- Kelime tablosunda 910 px altında kaynak, 820 px altında kelime türü, 730 px altında IPA sütunu gizlenir; genişlik artınca geri görünür.
- Ana pencere minimumu 760×560'tır. Ayarlar ve ilerleme ScrollPane içinde kalır.
- Dördüncü istatistik kartı tekrar zamanı gelmiş kelime sayısını gösterir.
- SQLite şeması 4 olarak kalır. Migration, kullanıcı verisi temizliği veya tekrar algoritması değişikliği yoktur.


## 2.3.0 geliştirme notları
- Kelime havuzuna dört durumlu öğrenme filtresi eklendi: tüm durumlar, henüz çalışılmayan, tekrar zamanı gelen ve öğrenilen.
- Henüz çalışılmayan: `last_reviewed_at IS NULL`; tekrar zamanı gelen: `next_due_at <= datetime('now')`; öğrenilen: `interval_days >= 21`.
- Sorgu parametreli ve salt okunurdur; SQLite şeması ve kullanıcı verileri değişmez.


## 2.4.0 geliştirme notları
- Toplu silme repository katmanında tek transaction kullanır ve `source = 'CUSTOM'` koşuluyla sınırlandırılır. Hazır katalog kayıtları silinemez.
- Rapor aralıkları yerel takvim tarihine göre bugün (1 gün), bugün dahil son 7 gün ve bugün dahil son 30 gün olarak hesaplanır.
- Raporlar `word_reviews` tablosundan okuma yapar; schema version 4 korunur.
- Filtre sıfırlama yalnızca arayüz seçimlerini temizler; veritabanına yazmaz.


## 2.6.0
- İstatistikler sekmesi adı ve rapor ekranı yenilendi.
- Bugün / Son 7 gün / Son 30 gün için çalışılan farklı kelime, toplam kart gösterimi, tamamlanan tekrar ve doğru yanıt metrikleri eklendi.
- Kelime kartı gösterimleri `word_presentations` tablosunda kalıcı olarak kaydedilir; eski `last_shown_at` verisi migration sırasında en az bir gösterim olarak aktarılır.


## 2.7.0 geliştirme notları
- `words.example_translation` cümlenin Türkçe karşılığını saklar; migration yalnızca yeni, boş varsayılanlı sütun ekler.
- `example-sentences.tsv` yerleşik kelime başlığına göre eşleştirilen üç alanlı (İngilizce başlık, İngilizce cümle, Türkçe çeviri) kaynak dosyasıdır. 103 başlangıç girdisi elle hazırlanmıştır.
- Seed işlemi yalnızca `source = 'BUILT_IN'` kayıtlarını günceller; kullanıcı kelimeleri etkilenmez.
- Hatırlatma kartında cümle çevirisi örnek cümlenin hemen altında görünür. Kelimenin Türkçe karşılığı ise aktif hatırlama akışını korumak için kullanıcı "Türkçe anlamı göster" dediğinde açılır.
- Yeni şema sürümü 7'dir. `word_reviews`, `word_presentations`, kelime sayaçları, kişisel kelimeler ve ayarlar migration sırasında silinmez.
- Örnek cümle ve çeviri kapsamı kademeli genişletilecektir; doğrulanmamış otomatik çeviri yerine boşluklar açıkça gösterilir.


## 2.8.0 geliştirme notları
- `example-sentences.tsv` toplam 203 doğrulanmış içerik satırına çıkarıldı.
- 2.7.0 paketindeki 103 içerik satırına ek olarak, mevcut İngilizce örnek cümlesi olan 100 yerleşik kelimeye Türkçe çeviri eklendi.
- Bu 100 satırdaki İngilizce cümleler katalogdaki mevcut cümlelerle birebir eşleştirildi; yalnızca Türkçe çeviri eklendi.
- `seedExampleSentences()` yalnızca `source = 'BUILT_IN'` kayıtlarını günceller. Kişisel kelimeler, tekrar geçmişi, istatistikler ve ayarlar etkilenmez.
- Bu sürümde veritabanı şeması değişmez (7).


## 2.9.0 geliştirme notları
- `example-sentences.tsv` toplam 303 içerik satırına çıkarıldı. 2.9.0 paketi, katalogda daha önce İngilizce örneği bulunmayan 100 günlük kelime için yeni cümle ve Türkçe çeviri ekler.
- 100 başlığın tamamı katalogda bulunur; eski seed dosyasında yer almaz ve katalog satırında önceden örnek cümle yoktur.
- 2.9.0 sonunda katalogda 814 İngilizce örnek cümle, 303 Türkçe cümle çevirisi ve 3.840 örneksiz kelime bulunur. 511 mevcut örneğin çevirisi henüz yoktur.
- Seed işlemi `source = 'BUILT_IN'` koşuluyla sınırlıdır. Özel kelimeler, tekrar geçmişi, gösterim geçmişi, sayaçlar ve ayarlar etkilenmez.
- Şema 7 olarak kalır; migration yapılmaz.
