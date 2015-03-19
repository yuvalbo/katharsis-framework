package io.katharsis.rs.controller;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.path.ResourcePath;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.ResourceResponse;
import io.katharsis.rs.controller.annotation.JsonInject;
import io.katharsis.rs.controller.hk2.JsonInjectResolver;
import io.katharsis.rs.controller.hk2.factory.RequestDispatcherFactory;
import io.katharsis.rs.controller.hk2.factory.ResourcePathFactory;
import io.katharsis.rs.controller.hk2.factory.ResourceRegistryFactory;
import io.katharsis.rs.resource.model.Task;
import io.katharsis.rs.resource.repository.ProjectRepository;
import io.katharsis.rs.resource.repository.TaskRepository;
import io.katharsis.rs.resource.repository.TaskToProjectRepository;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.GenericType;
import java.util.stream.StreamSupport;

public class JsonApiControllerTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.forServlet(
                new ServletContainer(new MyApplication())).build();
    }

    @Test
    public void onSimpleCollectionGetShouldReturnCollectionOfResources() {
        // WHEN
        CollectionResponse<Task> taskCollectionResponse = target("tasks/")
                .request()
                .get(new GenericType<CollectionResponse<Task>>() {
                });

        // THEN
        Assert.assertNotNull(taskCollectionResponse);
        Assert.assertNotNull(taskCollectionResponse.getData());
        Assert.assertEquals(1L, StreamSupport.stream(taskCollectionResponse.getData().spliterator(), false).count());
    }

    @Test
    public void onSimpleResourceGetShouldReturnOneResource() {
        // WHEN
        ResourceResponse<Task> taskResourceResponse = target("tasks/1")
                .request()
                .get(new GenericType<ResourceResponse<Task>>() {
                });

        // THEN
        Assert.assertNotNull(taskResourceResponse);
        Assert.assertNotNull(taskResourceResponse.getData());
        Assert.assertEquals(1L, (long) taskResourceResponse.getData().getId());
    }

    @ApplicationPath("/")
    private static class MyApplication extends ResourceConfig {

        public MyApplication() {
            register(JsonApiController.class);
            register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bindAsContract(ProjectRepository.class);
                    bindAsContract(TaskRepository.class);
                    bindAsContract(TaskToProjectRepository.class);
                    bindFactory(RequestDispatcherFactory.class).to(RequestDispatcher.class);
                    bindFactory(ResourcePathFactory.class).to(ResourcePath.class);
                    bindFactory(ResourceRegistryFactory.class).to(ResourceRegistry.class);

                    bind(JsonInjectResolver.class)
                            .to(new TypeLiteral<InjectionResolver<JsonInject>>() {
                            })
                            .in(Singleton.class);
                }
            });
        }
    }
}