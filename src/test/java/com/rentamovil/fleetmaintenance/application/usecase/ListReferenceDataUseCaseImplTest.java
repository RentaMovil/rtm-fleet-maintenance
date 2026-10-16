package com.rentamovil.fleetmaintenance.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rentamovil.fleetmaintenance.domain.entity.CatalogItem;
import com.rentamovil.fleetmaintenance.domain.entity.VehicleModel;
import com.rentamovil.fleetmaintenance.domain.port.out.ReferenceDataRepositoryPort;

@ExtendWith(MockitoExtension.class)
class ListReferenceDataUseCaseImplTest {

    @Mock
    private ReferenceDataRepositoryPort referenceDataRepositoryPort;

    @InjectMocks
    private ListReferenceDataUseCaseImpl listReferenceDataUseCase;

    @Test
    void listsBrandsFromTheRepository() {
        var toyota = new CatalogItem(1, "Toyota");
        when(referenceDataRepositoryPort.findAllBrands()).thenReturn(List.of(toyota));

        assertThat(listReferenceDataUseCase.listBrands()).containsExactly(toyota);
    }

    @Test
    void listsCategoriesFromTheRepository() {
        var sedan = new CatalogItem(2, "Sedan");
        when(referenceDataRepositoryPort.findAllCategories()).thenReturn(List.of(sedan));

        assertThat(listReferenceDataUseCase.listCategories()).containsExactly(sedan);
    }

    @Test
    void listsEngineTypesFromTheRepository() {
        var gasoline = new CatalogItem(3, "Gasoline");
        when(referenceDataRepositoryPort.findAllEngineTypes()).thenReturn(List.of(gasoline));

        assertThat(listReferenceDataUseCase.listEngineTypes()).containsExactly(gasoline);
    }

    @Test
    void listsVehicleModelsFromTheRepository() {
        when(referenceDataRepositoryPort.findAllVehicleModels()).thenReturn(List.of(TestData.COROLLA));

        assertThat(listReferenceDataUseCase.listVehicleModels()).containsExactly(TestData.COROLLA);
    }

    @Test
    void listsMaintenanceTypesFromTheRepository() {
        var oilChange = new CatalogItem(4, "Oil change");
        when(referenceDataRepositoryPort.findAllMaintenanceTypes()).thenReturn(List.of(oilChange));

        assertThat(listReferenceDataUseCase.listMaintenanceTypes()).containsExactly(oilChange);
    }
}
