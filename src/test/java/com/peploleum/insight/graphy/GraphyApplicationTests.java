package com.peploleum.insight.graphy;

import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.conversion.script.GremlinScriptLiteralVertex;
import com.microsoft.spring.data.gremlin.mapping.GremlinMappingContext;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import com.microsoft.spring.data.gremlin.repository.support.GremlinEntityInformation;
import com.peploleum.insight.graphy.domain.Relation;
import com.peploleum.insight.graphy.repository.NetworkRepository;
import com.peploleum.insight.graphy.service.*;
import com.peploleum.insight.graphy.web.rest.Type;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GraphyApplication.class)
public class GraphyApplicationTests {

    @Autowired
    private RelationServiceImpl relationService;

    @Autowired
    private BiographicsServiceImpl biographicsService;

    @Autowired
    private EventServiceImpl eventService;

    @Autowired
    private EquipmentServiceImpl equipmentService;

    @Autowired
    private OrganisationServiceImpl organisationService;

    @Autowired
    private RawDataServiceImpl rawDataService;

    @Autowired
    private LocationServiceImpl locationService;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GremlinFactory gremlinFactory;

    private GremlinTemplate template;

    private final Logger log = LoggerFactory.getLogger(GraphyApplicationTests.class);

    private String mongoId = UUID.randomUUID().toString();

    @Before
    public void setup() throws ClassNotFoundException {
        final GremlinMappingContext mappingContext = new GremlinMappingContext();

        mappingContext.setInitialEntitySet(new EntityScanner(this.context).scan(Persistent.class));

        final MappingGremlinConverter converter = new MappingGremlinConverter(mappingContext);

        this.template = new GremlinTemplate(this.gremlinFactory, converter);
    }

    @Test
    public void relationCreateTest() {
        createCustomGraph();

        this.template.findAll(new GremlinEntityInformation<>(Relation.class).createGremlinSource());
        Assert.assertEquals(Long.valueOf(20).longValue(), this.template.edgeCount());
        Assert.assertEquals(Long.valueOf(12).longValue(), this.template.vertexCount());
    }

    private void createCustomGraph() {
        //inner ring
        final Long biographicsId = this.biographicsService.save("Paul", mongoId);
        final String rawDataMongoId = UUID.randomUUID().toString();
        final Long rawDataId = this.rawDataService.save("Tweet", rawDataMongoId, "TWITTER");
        final String organisationMongoId = UUID.randomUUID().toString();
        final Long organisationId = this.organisationService.save("UN", organisationMongoId, "United Nations");
        final String equipementMongoId = UUID.randomUUID().toString();
        final Long equipmentId = this.equipmentService.save("Gun", equipementMongoId);
        final String eventMongoId = UUID.randomUUID().toString();
        final Long eventId = this.eventService.save("Meeting", eventMongoId);
        final String locationMongoId = UUID.randomUUID().toString();
        final Long locationId = this.locationService.save("Paris", locationMongoId);

        //outer ring
        final String outerBiographicsMongoId = UUID.randomUUID().toString();
        final Long outerBiographicsId = this.biographicsService.save("John", outerBiographicsMongoId);
        final String outerRawDataMongoId = UUID.randomUUID().toString();
        final Long outerRawDataId = this.rawDataService.save("CNN", outerRawDataMongoId, "RSS");
        final String outerOrganisationMongoId = UUID.randomUUID().toString();
        final Long outerOrganisationId = this.organisationService.save("ISIS", outerOrganisationMongoId, "Ismalic State");
        final String outerEquipementMongoId = UUID.randomUUID().toString();
        final Long outerEquipmentId = this.equipmentService.save("RPG", outerEquipementMongoId);
        final String outerEventMongoId = UUID.randomUUID().toString();
        final Long outerEventId = this.eventService.save("Bombing", outerEventMongoId);
        final String outerLocationMongoId = UUID.randomUUID().toString();
        final Long outerLocationId = this.locationService.save("London", outerLocationMongoId);

        //inner ring
        this.relationService.save(rawDataId, biographicsId, "linked to", Type.RawData, Type.Biographics);
        this.relationService.save(rawDataId, eventId, "linked to", Type.RawData, Type.Event);
        this.relationService.save(rawDataId, organisationId, "linked to", Type.RawData, Type.Organisation);
        this.relationService.save(rawDataId, equipmentId, "linked to", Type.RawData, Type.Equipment);
        this.relationService.save(rawDataId, locationId, "linked to", Type.RawData, Type.Location);

        //outer ring
        this.relationService.save(biographicsId, outerBiographicsId, "linked to", Type.Biographics, Type.Biographics);
        this.relationService.save(biographicsId, locationId, "linked to", Type.Biographics, Type.Location);
        this.relationService.save(biographicsId, equipmentId, "linked to", Type.Biographics, Type.Equipment);
        this.relationService.save(biographicsId, eventId, "linked to", Type.Biographics, Type.Event);
        this.relationService.save(biographicsId, biographicsId, "linked to", Type.Biographics, Type.Biographics);
        this.relationService.save(biographicsId, organisationId, "linked to", Type.Biographics, Type.Organisation);
        this.relationService.save(organisationId, outerBiographicsId, "linked to", Type.Organisation, Type.Biographics);
        this.relationService.save(organisationId, outerEventId, "linked to", Type.Organisation, Type.Event);
        this.relationService.save(eventId, outerOrganisationId, "linked to", Type.Event, Type.Organisation);
        this.relationService.save(equipmentId, outerEquipmentId, "linked to", Type.Equipment, Type.Equipment);
        this.relationService.save(locationId, outerOrganisationId, "linked to", Type.Location, Type.Organisation);
        this.relationService.save(locationId, outerEventId, "linked to", Type.Location, Type.Event);
        this.relationService.save(locationId, outerEquipmentId, "linked to", Type.Location, Type.Equipment);
        this.relationService.save(locationId, outerBiographicsId, "linked to", Type.Location, Type.Biographics);
        this.relationService.save(locationId, outerLocationId, "linked to", Type.Location, Type.Location);
    }

    @Test
    public void emptyCountTest() {
        Assert.assertEquals(Long.valueOf(0).longValue(), this.template.edgeCount());
        Assert.assertEquals(Long.valueOf(0).longValue(), this.template.vertexCount());
    }

    @Test
    public void customQueryTest() {
        createCustomGraph();
        Assert.assertEquals(Long.valueOf(20).longValue(), this.template.edgeCount());
        final String pauls = GremlinScriptLiteralVertex.generateHas("biographicsName", "John");

        final ResultSet resultSet = this.template.getGremlinClient().submit("g.V()." + pauls);
        this.log.info("searhing by name: John");
        resultSet.stream().forEach(result -> {
            final LinkedHashMap resultObject = (LinkedHashMap) result.getObject();
            resultObject.keySet().stream().forEach((key -> {
                this.log.info(key + " - " + resultObject.get(key).toString());
            }));
        });
        this.log.info("searching by MongoId: " + mongoId);
        final String mongos = GremlinScriptLiteralVertex.generateHas("idInsight", mongoId);
        final ResultSet mongosResultSet = this.template.getGremlinClient().submit("g.V()." + mongos);
        mongosResultSet.stream().forEach(result -> {
            final LinkedHashMap resultObject = (LinkedHashMap) result.getObject();
            resultObject.keySet().stream().forEach((key -> {
                this.log.info(key + " - " + resultObject.get(key).toString());
            }));
        });
        final String neighborSuffix = ".outE().limit(50).inV().toList()";
        final String mongoIdQuery = GremlinScriptLiteralVertex.generateHas("idInsight", mongoId);
        final ResultSet neighborResultSet = this.template.getGremlinClient().submit("g.V()." + mongoIdQuery + neighborSuffix);
        this.log.info("Searching Relations");
        neighborResultSet.stream().forEach(result -> {
            this.log.info("------------");
            final LinkedHashMap resultObject = (LinkedHashMap) result.getObject();
            resultObject.keySet().stream().forEach((key -> {
                this.log.info(key + " - " + resultObject.get(key).toString());
            }));
        });
    }

    @After
    public void remove() {
        this.networkRepository.deleteAll();
        this.template.deleteAll();
    }

}

