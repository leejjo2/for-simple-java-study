package com.example.forsimplestudy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonHandling {


    public static void main(String[] args) {

    }

}

@Getter
@Setter
class Address {
    private String city;
    private String street;

    // 생성자, getter, setter 등의 코드는 생략합니다.
}

@Getter
@Setter
class Person {
    private String name;
    private int age;
    private List<Address> addresses;

    // 생성자, getter, setter 등의 코드는 생략합니다.

    // Java 객체를 JSON 형식의 문자열로 변환하는 메서드
    public String toJsonString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

class JsonExample {
    public static void main(String[] args) throws JsonProcessingException {
        Address address1 = new Address();
        address1.setCity("Seoul");
        address1.setStreet("Gangnam-gu");

        Address address2 = new Address();
        address2.setCity("Busan");
        address2.setStreet("Haeundae-gu");

        List<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);

        Person person = new Person();
        person.setName("John");
        person.setAge(30);
        person.setAddresses(addresses);

        String jsonString = person.toJsonString();
        System.out.println(jsonString);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readValue(jsonString, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

        fields.forEachRemaining(stringJsonNodeEntry -> {
//            System.out.println(stringJsonNodeEntry.getKey()+" :: "+stringJsonNodeEntry.getValue());
//            System.out.println(stringJsonNodeEntry.getValue().getNodeType());
            JsonExample.getChild(stringJsonNodeEntry.getKey(), stringJsonNodeEntry.getValue());
        });
        System.out.println(fields);

    }

    public static void getChild(String key, JsonNode value){
        if(value.getNodeType() == JsonNodeType.NUMBER || value.getNodeType() == JsonNodeType.STRING){
            System.out.println(key + " :: " + value);
        }else{
            value.forEach(jsonNode -> {
                jsonNode.fields().forEachRemaining(stringJsonNodeEntry -> {
                    getChild(stringJsonNodeEntry.getKey(), stringJsonNodeEntry.getValue());
                });
            });
        }
    }


}