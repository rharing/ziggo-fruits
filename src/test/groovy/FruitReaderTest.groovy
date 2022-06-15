/**
 * Created on 6/15/22.
 */
class FruitReaderTest extends spock.lang.Specification {
    def " complete lifecycle"(){
        given:
        FruitReader fruitReader = new FruitReader()
        when:
        String content = fruitReader.read("resources/main/FruitExport_20220601.json")
        then:
        content =="""{
    "fruit": {
        "Banaan": {
            "rijp": 7,
            "groen": 5,
            "overrijp": 7
        }
    }
}"""
    }
    def "createBananenOutputMap"() {
        given:
        FruitReader fruitReader = new FruitReader()
        Fruit rijp = new Fruit(type:" Banaan",color:" geel", productstate: "rijp")
        Fruit groen = new Fruit(type:" Banaan", color:" bruin", productstate: "groen")
        Fruit overrijp = new Fruit(type:" Banaan", color:"groen", productstate: "overrijp")
        Map<Fruit, Integer> bananen =[:]
        bananen.put(rijp, 5)
        bananen.put(groen,3)
        bananen.put(overrijp,1)
        when:
        String json = fruitReader.createBananenOutputMap(bananen)
        then:
        json=="""{
    "fruit": {
        "Banaan": {
            "rijp": 5,
            "groen": 3,
            "overrijp": 1
        }
    }
}"""
    }
}
