package com.rentamovil.fleetmaintenance.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.port.in.ListReferenceDataUseCase;
import com.rentamovil.fleetmaintenance.infrastructure.web.dto.CatalogItemResponse;

/** GET /brands, /categories, /engine-types, /vehicle-models, /maintenance-types: public catalogs. */
class ReferenceDataControllerTest {

    private final ListReferenceDataUseCase useCase = mock(ListReferenceDataUseCase.class);
    private final ReferenceDataController controller = new ReferenceDataController(useCase);

    @Test
    void brandsAreMappedFromTheUseCase() {
        when(useCase.listBrands()).thenReturn(List.of(new CatalogItem(1, "Toyota")));

        assertThat(controller.brands()).containsExactly(new CatalogItemResponse(1, "Toyota"));
    }

    @Test
    void categoriesAreMappedFromTheUseCase() {
        when(useCase.listCategories()).thenReturn(List.of(new CatalogItem(2, "Sedan")));

        assertThat(controller.categories()).containsExactly(new CatalogItemResponse(2, "Sedan"));
    }

    @Test
    void engineTypesAreMappedFromTheUseCase() {
        when(useCase.listEngineTypes()).thenReturn(List.of(new CatalogItem(3, "Gasoline")));

        assertThat(controller.engineTypes()).containsExactly(new CatalogItemResponse(3, "Gasoline"));
    }

    @Test
    void vehicleModelsAreMappedFromTheUseCase() {
        var corolla = new VehicleModel(7, "Corolla", new CatalogItem(1, "Toyota"),
                new CatalogItem(2, "Sedan"), new CatalogItem(3, "Gasoline"));
        when(useCase.listVehicleModels()).thenReturn(List.of(corolla));

        assertThat(controller.vehicleModels()).extracting(r -> r.name()).containsExactly("Corolla");
    }

    @Test
    void maintenanceTypesAreMappedFromTheUseCase() {
        when(useCase.listMaintenanceTypes()).thenReturn(List.of(new CatalogItem(4, "Oil change")));

        assertThat(controller.maintenanceTypes()).containsExactly(new CatalogItemResponse(4, "Oil change"));
    }
}
