# WordReminder kelime verisi: kaynak ve lisans bildirimleri

Bu dosya uygulamayla dağıtılan `word-catalog.tsv` veri dosyasının kaynaklarını ve yeniden kullanım notlarını açıklar. Bu metin hukuki danışmanlık değildir.

## LexiCore-5000

- Kaynak: https://github.com/X-Trivle/LexiCore
- Veri: `data/LexiCore_5000.csv`; İngilizce A1-C1 başlık kelimeleri.
- Lisans: Creative Commons Attribution 4.0 International (CC BY 4.0).
- Atıf: LexiCore maintainers, *LexiCore-5000: an open, evidence-ranked English core vocabulary list*, release 1.0.0 (2026), https://github.com/X-Trivle/LexiCore.
- Not: Kaynağın kendi açıklaması bazı seviye etiketlerinin sınıflandırıcı tarafından tahmin edildiğini bildirir. Kaynak A1-C1 kapsamındadır; C2 etiketleri bu kaynaktan gelmez.

## FreeDict English-Turkish 0.3

- Kaynak: https://freedict.org/ ve https://sourceforge.net/projects/freedict/files/eng-tur/0.3/
- Veri: English-Turkish sözlük, 0.3 sürümü.
- Lisans bildirimi: GNU General Public License version 2.0 veya daha sonraki herhangi bir sürüm.
- Kaynak sürümün lisans bildirimi: https://dictionary.catflap.org/?Database=freedict%3Aeng-tur&Form=%2F3
- Not: Sözlükteki İngilizce-Türkçe eşleştirmeler ana katalogda kullanılmıştır. GPL-2.0 lisans metninin kopyası `THIRD-PARTY-LICENSES/GPL-2.0.txt` içindedir. FreeDict kaynak sürümünün ve yeniden dağıtım yükümlülüklerinin ayrıca incelenmesi gerekir.

## CMU Pronouncing Dictionary

- Kaynak: https://github.com/cmusphinx/cmudict
- Veri: İngilizce kelimelerin ABD İngilizcesi telaffuz kayıtları.
- Telif/lisans bildirimi: Carnegie Mellon University, 1993-2015; yeniden dağıtım ve kullanım koşulları BSD benzeri izin metniyle düzenlenmiştir.
- Lisans metni: `THIRD-PARTY-LICENSES/CMU-PRONOUNCING-DICTIONARY.txt`.
- Kullanım: CMU ARPABET telaffuz kayıtlarının katalogdaki karşılıkları, bu projeye ait dönüştürme işlemiyle geniş (broad) ABD IPA gösterimine çevrilmiştir. CMUdict yazılım paketi veya dönüştürme aracı uygulamaya eklenmemiştir; katalogda yalnızca kelime telaffuz verisinin bir alt kümesi ve dönüştürülmüş gösterimleri bulunur.
- Sınırlama: Telaffuz alanı 4.654 kelimenin 4.582'sinde doldurulmuştur. Kaynakta veya çözümlenebilen kelime bileşenlerinde karşılık bulunmayan 72 kelimenin telaffuzu boş bırakılmıştır. Birleşik ifadelerde, mümkün olduğunda sözcük telaffuzları birleştirilmiştir; bu kayıtlar ifade düzeyinde vurgu/bağlantı analizi değildir.

## WordReminder için özgün teknik terimler ve örnek cümleler

Yazılım mühendisliği kategorisine eklenen teknik terim adları, bağlama uygun Türkçe karşılık düzeltmeleri ve proje örnek cümleleri bu proje için özgün olarak hazırlanmıştır; başka bir teknik sözlük dosyasından toplu olarak kopyalanmamıştır. CEFR seviyeleri ve kelime türleri öğrenmeyi desteklemek için yaklaşık sınıflandırmalardır.

## Atıf ve değişiklikler

- LexiCore verisi CC BY 4.0 atıf koşullarına tabidir.
- FreeDict verisi kendi GPL lisans bildirimiyle dağıtılır; FreeDict'e dayalı içerik kullanılırken ilgili GPL koşulları göz ardı edilmemelidir.
- CMUdict telaffuz verisi için atıf ve lisans metni bu projeyle birlikte dağıtılır.
- `word-catalog.tsv` birleştirilmiş ve düzenlenmiş bir veri kümesidir. Kaynak lisansları ve yeniden dağıtım yükümlülükleri dağıtım öncesinde incelenmelidir.
