package book.shop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // 값 타입은 Setter 를 제공하지 않아 변경이 불가능하게 해야한다.
@Getter
public class Address {

    private String city;

    private String street;

    private String zipcode;

    protected Address() { }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
