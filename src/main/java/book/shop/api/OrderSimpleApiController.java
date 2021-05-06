package book.shop.api;

import book.shop.domain.Address;
import book.shop.domain.Order;
import book.shop.domain.OrderSearch;
import book.shop.domain.OrderStatus;
import book.shop.repository.OrderRepository;
import book.shop.repository.order.simplequery.OrderSimpleQueryDto;
import book.shop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * x To One ( ManyToOne, OneToOne )
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    private final OrderSimpleQueryRepository orderSimpleQueryRepository;


    /**
     * 쓰면 안되는 최악의 방법 : 엔티티 직접 노출
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기화
        }
        return all;
    }

    /**
     * N + 1 문제가 발생함
     * 쿼리가 총 1 + N + N번 실행된다. (v1과 쿼리수 결과는 같다.)
     * order 조회 1번(order 조회 결과 수가 N이 된다.)
     * order -> member 지연 로딩 조회 N 번
     * order -> delivery 지연 로딩 조회 N 번
     */
    @GetMapping("/api/v2/simple-orders")
    public List<OrderSimpleDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderSimpleDto(o))
                .collect(Collectors.toList());
    }

    /**
     * fetch join 을 이용한 성능 최적화
     */
    @GetMapping("/api/v3/simple-orders")
    public List<OrderSimpleDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new OrderSimpleDto(o))
                .collect(Collectors.toList());
    }

    /**
     * V3 와 같이 1번의 쿼리가 나가지만 select 쿼리를 직접 입력했기 때문에
     * select 절에서 차이를 보인다.
     * 하지만 재사용성이 떨어지고,
     * API 스펙에 Fit 한 코드가 리포지토리에 들어가게 된다.
     * 또한 network bandwidth 가 좋기 때문에 성능 면에서도 별 차이가 없다.
     * 그러므로, V3 와 trade - off 가 존재한다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class OrderSimpleDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public OrderSimpleDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
