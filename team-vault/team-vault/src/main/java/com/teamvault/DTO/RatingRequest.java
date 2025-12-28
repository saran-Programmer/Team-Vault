package com.teamvault.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RatingRequest {

    @NotNull(message = "Rating is required")
    @Pattern(regexp = "^[1-5](\\.(0|1|2|3|4|5))?$",message = "Allowed values: 1.0 to 5.5 in increments of .0 to .5")
    private String rating;
    
    public Double getRating() {
    	
    	return Double.valueOf(this.rating);
    }
}