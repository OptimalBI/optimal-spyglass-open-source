package com.optimalbi.GUI;

import com.optimalbi.Services.Service;
import javafx.scene.control.Label;
import org.apache.commons.lang.Validate;

public class s3QueueObject {
    private final Service s3Service;
    private final Label objectCount;
    private final Label size;

    public s3QueueObject(Service s3Service,Label objectCount, Label size){
        Validate.noNullElements(new Object[]{s3Service,objectCount,size});
        this.s3Service = s3Service;
        this.objectCount = objectCount;
        this.size = size;
    }

    public Service getS3Service() {
        return s3Service;
    }

    public Label getObjectCount() {
        return objectCount;
    }

    public Label getSize() {
        return size;
    }
}
