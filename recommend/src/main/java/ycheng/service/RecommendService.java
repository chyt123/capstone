package ycheng.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ycheng.database.LocalFileStorage;
import ycheng.processor.RecommendAlgorithm;
import ycheng.processor.RecommendAlgorithmFactory;
import ycheng.tester.AlgorithmTester;
import ycheng.tester.RecommendTestFactory;
import ycheng.tester.TestResult;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

/**
 * Created by ycheng on 9/16/17.
 */
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/")
public class RecommendService {
    private static ObjectMapper mapper = new ObjectMapper();

    @POST
    @Path("recommend")
    public Map getRecommendations(RecommendData data) {
        RecommendAlgorithmFactory factory = new RecommendAlgorithmFactory();
        RecommendAlgorithm algo = factory.getAlgo(data.getAlgo());
        if (data.getSourceData() == null) throw new WebApplicationException(Response.Status.BAD_REQUEST);
        Date start = new Date();
        List<Integer> res = algo.recommend(data.getSourceData(), data.getRecommendNum());
        Date end = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("time", end.getTime() - start.getTime());
        map.put("res", res);
        return map;
    }

    @POST
    @Path("train")
    public Map train(@QueryParam("algo") String algoS) {
        RecommendAlgorithmFactory factory = new RecommendAlgorithmFactory();
        RecommendAlgorithm algo = factory.getAlgo(algoS);
        Date start = new Date();
        try {
            algo.train(LocalFileStorage.read(LocalFileStorage.SOURCE_BUY_DATA),
                    LocalFileStorage.read(LocalFileStorage.SOURCE_CLICK_DATA));

        } catch (IOException e) {
            e.printStackTrace();
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        Date end = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("time", end.getTime() - start.getTime());
        return map;
    }

    @POST
    @Path("test")
    public TestResult test(TestRequest data) {
        RecommendTestFactory factory = new RecommendTestFactory();
        AlgorithmTester tester = factory.getTester(data.getAlgo());
        tester.preprocess(data.getTestFile());
        Map<String, Object> map = mapper.convertValue(data, Map.class);
        TestResult r = tester.test(map);
        return r;
    }
}
