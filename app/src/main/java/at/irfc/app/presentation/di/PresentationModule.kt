package at.irfc.app.presentation.di

import at.irfc.app.presentation.map.MapViewModel
import at.irfc.app.presentation.program.ProgramViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::ProgramViewModel)
    viewModelOf(::MapViewModel)
}
