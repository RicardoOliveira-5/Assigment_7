package pt.unl.fct.iadi.novaevents.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import pt.unl.fct.iadi.novaevents.service.WeatherService

@Controller
@RequestMapping("/api/weather")
class WeatherController(
    private val weatherService: WeatherService
) {

    @GetMapping(produces = ["application/json"])
    @ResponseBody
    fun getWeatherJson(@RequestParam location: String): Map<String, Boolean?> {
        val raining = weatherService.isRaining(location)
        return mapOf("raining" to raining)
    }

    @GetMapping(produces = ["text/html"])
    fun getWeatherHtml(
        @RequestParam location: String,
        model: Model
    ): String {

        val raining = weatherService.isRaining(location)

        model.addAttribute("raining", raining)

        return "fragments/weather :: result"
    }
}