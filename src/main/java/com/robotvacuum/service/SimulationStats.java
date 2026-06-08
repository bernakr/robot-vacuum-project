package com.robotvacuum.service;

// Verilein düzenli tutulup hesap kısmından sonra hemen veri döndürmemek için bunu kullandık
//Staticservices içinde hespalamalr ypaıplıp burada taşınılıyor kullanılması için
// sağ alttaki alanların ve sabit değerlerin verilerini tutmka için oluşturulmuş ypaımız burada 
// Record kullanmamızın sebei de sadece değer tutup bu değerlerle iligli bir işlem olmadığı için record türünde tuttuk  sınıf türünde yani 
public record SimulationStats(
        int totalArea,
        int cleanableArea,
        int obstacleArea,
        int remainingDirt,
        int remainingDirtyArea,
        int remainingCleanableArea,
        int visitedCleanableArea,
        double cleanedPercentage,
        double remainingCleanablePercentage,
        double dirtPercentage,
        double collectedDirtPercentage
) {
}
