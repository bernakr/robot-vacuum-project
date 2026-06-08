package com.robotvacuum.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.robotvacuum.model.Position;
import com.robotvacuum.model.Room;
import com.robotvacuum.model.enums.Direction;

public class BfsPathfindingService {

    // BFS algoritmasının ana çalışan kısmı
    // start: robotun başladığı konum
    // goal: ulaşılmak istenen hedef
    // allowedChargingStationGoal: hedef şarj istasyonuysa oraya girişe izin verir
    private List<Position> findPath(
            Room room,
            Position start,
            Position goal,
            Position allowedChargingStationGoal) {
        // Başlangıç veya hedef geçilemezse yol yok kabul edilir
        if (!room.isPassableForPath(start, start)
                || !room.isPassableForPath(goal, allowedChargingStationGoal)) {
            return List.of();
        }

        // BFS sırasında gezilecek pozisyonları tutan kuyruk
        Queue<Position> queue = new ArrayDeque<>(); // kuyruk mantığı çünkü geldiği son noktadan bşalngıça dönüyor 
        // üst kısımdan alta  doğru çıkarım gibi 
        
        // Daha önce kontrol edilen pozisyonlar
        // Aynı hücreye tekrar tekrar girmeyi engeller
        Set<Position> visited = new HashSet<>();

        // Her pozisyona nereden gelindiğini tutar
        // Yolun sonunda geriye doğru rota çıkarmak için kullanılır
        Map<Position, Position> previous = new HashMap<>();

        // Başlangıç pozisyonu kuyruğa alınır
        queue.add(start);
        visited.add(start);

        // Kuyruk boşalana kadar komşular taranır
        while (!queue.isEmpty()) {

            // Kuyruğun başındaki pozisyon alınır
            Position current = queue.poll();

            // Hedefe ulaşıldıysa rota geri kurulur
            if (current.equals(goal)) {
                return reconstructPath(previous, start, goal);
            }

            // 4 yön kontrol edilir: yukarı, sağ, aşağı, sol gibi
            for (Direction direction : Direction.values()) {

                // Mevcut pozisyondan ilgili yöne gidilirse oluşacak yeni pozisyon
                Position next = current.move(direction);

                // Hücre geçilebilir mi ve daha önce ziyaret edilmemiş mi kontrol edilir
                if (room.isPassableForPath(next, allowedChargingStationGoal)
                        && visited.add(next)) {

                    // next pozisyonuna current üzerinden ulaşıldığını kaydediyoruz
                    previous.put(next, current);

                    // next pozisyonunu daha sonra incelemek için kuyruğa ekliyoruz
                    queue.add(next);
                }
            }
        }

        // Hedefe ulaşılacak yol bulunamazsa boş liste döner
        return List.of();
    }

    // Normal hedefe giden yolu bulur
    // Örneğin robotun bir kire veya boş hücreye gitmesi için kullanılır
    public List<Position> findPath(Room room, Position start, Position goal) {
        return findPath(room, start, goal, null);
    }

    // Robotun şarj istasyonuna dönüş yolunu bulur
    // Şarj istasyonu normalde temizlik sırasında geçilebilir sayılmayabilir
    // Bu yüzden hedef olarak şarj istasyonuna özel izin verilir
    public List<Position> findPathToChargingStation(Room room, Position start) {
        return findPath(
                room,
                start,
                room.getChargingStation().getPosition(),
                room.getChargingStation().getPosition());
    }

    // BFS bittikten sonra hedef pozisyondan başlangıca doğru geri giderek yolu
    // oluşturur
    private List<Position> reconstructPath(
            Map<Position, Position> previous,
            Position start,
            Position goal) {
        List<Position> path = new ArrayList<>();

        // Geriye doğru hedef konumdan başlıyoruz
        Position current = goal;
        path.add(current);

        // Başlangıca ulaşana kadar previous map üzerinden geri gideriz
        while (!current.equals(start)) {
            current = previous.get(current);

            // Eğer bağlantı kopuksa yol kurulamaz
            if (current == null) {
                return List.of();
            }

            path.add(current);
        }

        // Şu an yol tersten kuruldu:
        // goal -> ... -> start
        // Bunu start -> ... -> goal haline getiriyoruz
        Collections.reverse(path);

        return path;
    }
}