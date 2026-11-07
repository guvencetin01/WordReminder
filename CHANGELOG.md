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
