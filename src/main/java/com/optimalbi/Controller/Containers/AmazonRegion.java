/*
   Copyright 2015 OptimalBI

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.optimalbi.Controller.Containers;

import com.amazonaws.regions.Region;

/**
 * Created by Timothy Gray(timg) on 2/12/2014..
 * Version: 1.0.0
 */
public class AmazonRegion {
    private final Region region;
    private boolean active;

    public AmazonRegion(Region region, boolean active){
        this.region = region;
        this.active = active;
    }

    public Region getRegion(){return region;}

    public boolean getActive(){return active;}

    public void toggleActive(){
        active = !active;
    }
}
