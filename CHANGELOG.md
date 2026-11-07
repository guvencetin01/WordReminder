## [3.6.0] - Örnek doğruluğu ve hatırlatma kartı düzeni

- Tekrarlanan yapay cümle kalıpları, bağlama ve hedef kelimenin kullanımına uygun örneklerle değiştirildi.
- Tespit edilen hatalı Türkçe çeviriler düzeltildi; seçili yaygın kelimelerin sözlük anlamları ve kelime türleri gözden geçirildi.
- Kart eylemleri tek satırda "Öğrendim", "Devam Et", "Zor Kelimelere Ekle" sırasıyla düzenlendi.
- "5 dk sonra" butonu ve yalnızca bu eyleme ait SNOOZE akışı kaldırıldı. "Devam Et" normal ayarlı tekrar aralığını kullanır.
- SQLite şeması 7 olarak kalır; migration veya kullanıcı verisi temizliği yapılmaz.

## [3.5.0] - Tüm kelimelere örnek cümle

- Katalogdaki 4.654 benzersiz kelimenin tamamı için en az bir İngilizce örnek cümle kapsamı sağlandı; A1–C2 seviyeleri ile günlük ve teknik gruplar tamamlandı.
- `example-sentences.tsv` dosyası 4.343 benzersiz kelimeye ait İngilizce örnek ve Türkçe çeviri içerir.
- Katalogda önceden bulunan 614 örnek de dâhil edildiğinde İngilizce örnek kapsamı 4.654/4.654 olur. Mevcut katalog örneklerinin 311'i ayrı Türkçe çeviri kaydı olmadan gösterilir.
- Yinelenen anahtar, katalog dışı başlık, boş alan ve hedef kelimeyi içermeyen cümle denetimleri temiz geçti.
- SQLite şeması 7 olarak kalır; migration, kullanıcı verisi sıfırlama veya silme yapılmadı.

## [3.4.0] - A2 örnek cümleleri

- A2 seviyesindeki 25 günlük kelimeye doğal İngilizce örnek cümle ve Türkçe çeviri eklendi.
- İçerik paketi 749 örnek/çeviri girdisine ulaştı; etkili katalogda 1.060 örnek cümle bulunur.
- Veritabanı şeması 7 olarak kalır; migration yoktur.

## [3.3.0] - Hatırlatma zamanlaması ve kart yaşam döngüsü

- Hatırlatma aralığı kaydedilmiş ayara tam olarak uyar; varsayılan değer 10 dakikadır. Ayar değişikliği zamanlayıcıya hemen uygulanır. “Tekrar et” eylemi de bu ayarı kullanır; yalnızca açıkça seçilen “5 dk sonra” eylemi 5 dakikalık tek seferlik ertelemedir.
- Aynı anda yalnızca bir kart gösterilir. Aralık açık kart sırasında dolarsa tek bekleyen hatırlatma kart kapanınca gösterilir; tekrar tekrar ertelenmez.
- Kart, açıldıktan 2 dakika sonra otomatik kapanır.
- Kullanıcının hiç etkileşmediği ve zaman aşımıyla kapanan kart gösterim istatistiğine eklenmez. Etkileşim varsa gösterim yalnızca bir kez kaydedilir.
- Veritabanı şeması 7; migration yoktur.

## [3.2.0] - Kart tasarımı, başlangıç ayarları ve örnek cümleler

- Windows başlangıç seçeneklerinin açıklamaları ayrı metin alanlarında gösterilir; sistem tepsisinde başlatma yalnızca otomatik başlatma açıkken seçilebilir.
- Hatırlatma kartı açık yeşil arka plan, ince koyu yeşil kenarlık ve uyumlu buton/metin renkleriyle yeniden tasarlandı.
- Zor kelime işareti kart kapanmadan açılıp kapatılır; diğer hatırlatma kararları kartı kapatır.
- 121 günlük kelimeye İngilizce örnek ve Türkçe çeviri eklendi; içerik dosyası 724 satıra ulaştı.
- Etkili katalog 1.035 İngilizce örnek ve 724 Türkçe çeviri içerir; 311 mevcut örnek çeviri bekler.
- SQLite şeması 7 olarak kalır; kullanıcı verileri korunur.

## [3.1.0] - Zamanlama güvenilirliği ve içerik genişletmesi

- Hatırlatma için kelime seçimi, kart gösterim kaydı ve tekrar/öğrenildi kayıtları veritabanı yürütücüsüne taşındı; JavaFX arayüz iş parçacığında senkron SQLite sorgusu/işlemi yapılmaz.
- Zaman kritik hatırlatma sorguları, tablo/istatistik işleri ve kullanıcı işlemlerinden bağımsız tek iş parçacıklı bir veritabanı yürütücüsünde çalışır.
- Ana pencerenin kelime tablosu araması ve istatistik yenilemesi arka planda çalışır. Arama 250 ms gecikmeyle toplulaştırılır; istek sıra numaraları eski sorgu sonuçlarının yeni sonuçları ezmesini engeller.
- İlk katalog seed işlemi uygulama penceresini bloke etmeden arka planda yürütülür. Normal hatırlatma sayacı seed tamamlandıktan sonra başlar.
- Kelime gösterim geçmişi Stage.show() başarılı olduktan sonra ayrı transaction ile yazılır; kuyruğa alınan veya gösterilemeyen popup'lar gösterim istatistiğini artırmaz.
- Hatırlatma günlüklerinde veritabanı iş kuyruğu, sorgu, JavaFX kuyruğu ve popup oluşturma/gösterim süreleri ayrı ölçülür.
- Mevcut İngilizce örnek cümlesi bulunan 100 teknik terime Türkçe çeviri eklendi. Katalogda 914 İngilizce örnek, 603 Türkçe çeviri ve 311 çevirisi bekleyen örnek bulunur.
- ReminderScheduler için anlık tetikleme, ertelemenin eski görevi değiştirmesi ve kapatıldığında bekleyen görevin iptali testleri eklendi.
- SQLite şema sürümü 7 olarak kalır; migration yoktur. Kişisel kelimeler, öğrenme ve gösterim geçmişi, sayaçlar ve ayarlar korunur.
## [3.0.0] - Hatırlatma zamanlaması ve içerik genişletmesi

- Daha önce İngilizce örneği bulunan 100 kelimenin Türkçe cümle çevirisi eklendi.
- Örneği olmayan 100 A1–A2 günlük kelime için yeni İngilizce örnek ve Türkçe çeviri eklendi.
- İçerik kaynağı 503 benzersiz kelime-cümle-çeviri çiftine ulaştı; katalogdaki örnek sayısı 914 oldu.
- Düzenli hatırlatma aralığı artık kartın gösterildiği anda başlar; kartın açık kaldığı süre tekrar aralığına eklenmez.
- Açık kart varken yeni pencere açılmaz; zamanlayıcı sonraki uygun aralığı bekler.
- Pencerenin işletim sistemi tarafından kapatılması da DISMISS olarak işlenir; etkin kart referansının takılı kalması önlenir.
- Gereksiz tablo/istatistik yenilemesi, yalnızca erteleme veya kapatma işlemlerinde kaldırıldı.
- Başka ayarlar kaydedilirken hatırlatma aralığı değişmemişse bekleyen sayaç artık sıfırlanmaz.
- Zamanlayıcı ve JavaFX olay kuyruğu gecikmeleri için dönen tanılama günlüğü eklendi.
- Eski kullanıcı verileri için migration gerekmez; SQLite şema sürümü 7 olarak kalır.

# Değişiklik günlüğü

## [2.9.0] - Yeni örnek cümle paketi

- Daha önce örneği bulunmayan 100 günlük kelime için elle hazırlanmış İngilizce cümle ve Türkçe çeviri eklendi.
- `example-sentences.tsv` toplam 303 içerik girdisine ulaştı.
- Toplam İngilizce örnek sayısı 814 oldu; 303 cümlenin Türkçe çevirisi bulunuyor.
- Veritabanı şeması 7 olarak kaldı; mevcut kullanıcı verileri korunur.

## [2.8.0] - Örnek cümle çevirilerinin genişletilmesi

- İçerik paketi 103 girdiden 203 girdiye çıkarıldı.
- Daha önce İngilizce örneği bulunan 100 kelimeye Türkçe cümle çevirisi eklendi.
- Yalnızca yerleşik kelimeler güncellenir; kişisel kelimeler korunur.
- Yeni SQLite migration'ı yoktur; şema 7 olarak kalır.

## [2.7.0] - Örnek cümleleri ve kart deneyimi

- Hatırlatma kartı profesyonel, kaydırılabilir bir düzene taşındı.
- Örnek cümlenin hemen altında Türkçe çevirisi gösteriliyor.
- Kişisel kelimelerde örnek cümle ve çeviri düzenlenebiliyor.
- 103 kelime için ilk elle hazırlanmış cümle/çeviri paketi eklendi.
- SQLite şema 7'ye yükseltildi; öğrenme verileri korunuyor.

## 2.6.0
- “Öğrenme ilerlemesi” sekmesi “İstatistikler” olarak yeniden adlandırıldı.
- Bugün, son 7 gün ve son 30 gün için çalışılan farklı kelime, toplam kelime kartı gösterimi, tamamlanan tekrar, öğrenildi sayısı ve doğru yanıt oranı eklendi.
- Tekrar sayısı kart gösteriminden ayrı tutulur: tekrar yalnızca “Öğrendim” veya “Tekrar et” kararlarını sayar.
- Her hatırlatma kartı gösterimi `word_presentations` tablosuna kaydedilir.
- SQLite şeması 6'ya yükseltildi; eski `last_shown_at` değerleri bir başlangıç gösterimi olarak aktarılır. Önceki öğrenme geçmişi, kişisel kelimeler ve ayarlar korunur.
- İstatistik ekranı açıklamalar, dönem kartları, günlük hedef ve CEFR ilerleme bölümleriyle zenginleştirildi.

## 2.5.0
- Kelime havuzunun öğrenme durumu filtresine “Çalışılan Kelimeler” ve “Zor Kelimeler” seçenekleri eklendi.
- “Çalışılan Kelimeler”, hatırlatma kartı en az bir kez gösterilmiş kelimeleri `last_shown_at` üzerinden listeler.
- Hatırlatma kartına “Zor Kelimelere Ekle / Zor Kelimelerden Çıkar” düğmesi eklendi; seçim SQLite'ta kalıcı saklanır.
- Kelime havuzunda zor kelimeler, başlıksız dar bir sütunda `*` işaretiyle gösterilir.
- `is_hard` alanı için SQLite şema sürümü 5 migration'ı eklendi; mevcut öğrenme geçmişi, kişisel kelimeler ve ayarlar korunur.
- Kelime havuzunda çoklu seçim etkinleştirildi; seçili kişisel kelimeler tek onayla toplu silinebilir. Yerleşik katalog toplu silme kapsamı dışındadır.
- Arama, CEFR, alan ve öğrenme durumu filtrelerini tek işlemle sıfırlama düğmesi eklendi.
- Öğrenme ilerlemesine bugün, son 7 gün ve son 30 gün için tekrar sayısı, öğrenildi sayısı ve doğru yanıt yüzdesini gösteren rapor kartları eklendi.
- Raporlar mevcut `word_reviews` kayıtlarından salt okunur hesaplanır; tekrar algoritması değişmez.
- Windows paketleme betiği 2.5.0 sürümüne güncellendi.
- Normal güncellemede kişisel kelimeler, öğrenme geçmişi ve ayarlar korunur.

## 2.3.0
- Kelime havuzuna öğrenme durumu filtresi eklendi: henüz çalışılmayan, tekrar zamanı gelen ve öğrenilmiş kelimeler.
- Filtreler mevcut `last_reviewed_at`, `next_due_at` ve `interval_days` alanlarını kullanır; yeni veritabanı alanı veya migration gerekmez.
- Öğrenme geçmişi, kişisel kelimeler, ayarlar ve yedekleme biçimi korunur.

## 2.2.0
- Ana pencere başlığı ve kelime işlemleri araç çubukları dar pencerelerde satır kaydırır.
- Ayarlar ekranı iki sütunlu kart düzenine geçti; açıklamalar dar alanda satır kaydırır.
- Kelime tablosu, dar pencerelerde ikincil IPA/tür/kaynak sütunlarını otomatik gizler; pencere genişletilince geri gösterir.
- İstatistik kartlarına tekrar zamanı gelen kelime sayısı eklendi.
- Minimum pencere boyutu 760×560 olarak ayarlandı; veritabanı ve öğrenme mantığı değişmedi.

## 2.1.0
- Ayarlar ve öğrenme ilerlemesi sekmelerine dikey kaydırma eklendi.
- Ayarları kaydet düğmesi üst araç satırına taşındı.
- Yedekleme/doğrulama ve öğrenme kapsamı eylemleri dar pencerelerde satır kaydırır.
- Minimum ana pencere boyutu 820×600 olarak ayarlandı.

## 2.0.0
- Ayarlar sekmesine yedek doğrulama eklendi.
- SQLite bütünlük kontrolü, şema sürümü ve kayıt sayıları raporlanır.
- Doğrulama salt okunur; canlı veritabanına dokunmaz.

# Sürüm geçmişi

## 1.9.0
- Ayarlar sekmesine tam SQLite veritabanı yedekleme özelliği eklendi.
- Yedek, `VACUUM INTO` ile kelimeleri, kişisel kelimeleri, tekrar geçmişini ve ayarları tutarlı tek dosyada saklar.
- Canlı veritabanı ve şema değiştirilmez; kullanıcı verileri korunur.


## 1.8.0
- Öğrenme ilerlemesi ekranına günlük öğrenme serisi eklendi.
- Seri, bugünün veya dünün tamamlanmış tekrarından başlayarak kesintisiz günleri sayar.
- Günlük seri mevcut word_reviews kayıtlarından hesaplanır; SQLite şeması ve kullanıcı verileri değişmez.

## 1.7.0
- Ayarlara kişiselleştirilebilir günlük tekrar hedefi (1–100) eklendi; varsayılan hedef 10.
- Öğrenme ilerlemesi ekranında bugünkü tamamlanan tekrar sayısı ve hedef yüzdesi gösterilir.
- Günlük sayaç, yerel takvim günündeki gerçek KNOWN/REPEAT inceleme kayıtlarını sayar; erteleme ve pencereyi kapatma sayılmaz.
- Hedef ayarı mevcut app_settings tablosunda saklanır; SQLite şeması ve geçmiş kayıtlar değişmez.


## 1.6.0
- Hatırlatma penceresine aktif hatırlama akışı eklendi: İngilizce kelime önce tek başına gösterilir, Türkçe anlam kullanıcı istediğinde açılır.
- Kullanıcı anlamı gördükten sonra "Öğrendim" veya "Tekrar et" kararını verir; mevcut uyarlanabilir tekrar algoritması aynen kullanılır.
- Anlamı açmadan pencereyi kapatma ve erteleme davranışları mevcut inceleme kayıtlarını değiştirmez.
- SQLite şeması değiştirilmedi; öğrenme geçmişi, kişisel kelimeler ve ayarlar korunur.


## 1.5.0
- Kelime havuzuna örnek cümlesi eksik kelimeleri filtreleme seçeneği eklendi.
- Öğrenme ilerlemesi özetine örnek cümlesi kapsamı eklendi; katalog kapsamı şeffaf biçimde görülebilir.
- CEFR ilerleme çubuklarının sıfır ilerleme durumunda da görünür olması için görsel erişilebilirlik iyileştirildi.
- SQLite öğrenme geçmişi ve kullanıcı ayarları için veri sıfırlama veya şema değişikliği yapılmadı.

## 1.4.0
- Sabit tekrar aralıkları yerine SM-2 esinli uyarlanabilir tekrar planı eklendi.
- Doğru yanıtta aralık 1 gün, ardından 3 gün; sonraki aralıklar kolaylık katsayısına göre hesaplanır ve 365 günle sınırlandırılır.
- Tekrar edilmesi gereken kelime 10 dakika sonrasına planlanır; kolaylık katsayısı düşürülür ve öğrenme aralığı sıfırlanır.
- CEFR seviyelerine göre öğrenme ilerlemesi, öğrenilmiş kelime sayısı ve doğru yanıt oranı gösterilir.
- SQLite şeması 4. sürüme yükseltildi; mevcut 1.3.0 öğrenme verileri korunarak yeni alanlar eklenir.

## 1.3.0
- Kelime türü, ABD IPA telaffuzu ve zenginleştirilmiş örnek cümleler.
- 4.654 kelimelik katalog.


## [2.7.0] - Örnek cümleleri ve kart deneyimi

- Hatırlatma kartı profesyonel, kaydırılabilir bir düzene taşındı.
- Örnek cümlenin hemen altında Türkçe çevirisi gösteriliyor.
- Kişisel kelimelerde örnek cümle ve çeviri düzenlenebiliyor.
- 103 kelime için ilk elle hazırlanmış cümle/çeviri paketi eklendi.
- SQLite şema 7'ye yükseltildi; öğrenme verileri korunuyor.
