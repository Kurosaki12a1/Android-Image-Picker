package vn.kuro.module_image_picker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import vn.kuro.module_image_picker.domain.repository.ImagePickerRepository
import vn.kuro.module_image_picker.domain.usecase.GetAlbumsUseCase
import vn.kuro.module_image_picker.domain.usecase.GetPhotosUseCase

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    @ViewModelScoped
    fun provideGetAlbumsUseCase(repo: ImagePickerRepository): GetAlbumsUseCase {
        return GetAlbumsUseCase(repo)
    }

    @Provides
    @ViewModelScoped
    fun provideGetPhotosUseCase(repo: ImagePickerRepository): GetPhotosUseCase {
        return GetPhotosUseCase(repo)
    }
}