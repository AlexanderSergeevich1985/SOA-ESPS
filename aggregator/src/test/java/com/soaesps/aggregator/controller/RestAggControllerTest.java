package com.soaesps.aggregator.controller;

import com.soaesps.aggregator.client.IWorkerNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import service.ILoadBalancer;
import service.LoadBalancer;
import service.Registry;

@RunWith(MockitoJUnitRunner.class)
public class RestAggControllerTest {
    @Rule
    public TestRule timeout = new Timeout(1000);

    @Mock
    private IWorkerNode node1;

    @Mock
    private IWorkerNode node2;

    //@InjectMocks
    private Registry registry;

    @Spy
    private ILoadBalancer balancer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.registry = new Registry();
        Registry registry = new Registry();
        registry.register(node1);
        registry.register(node2);
        this.balancer = Mockito.spy(new LoadBalancer(registry));
    }

    @Test
    public void testGetInstance() {
        ///this.balancer
    }
}