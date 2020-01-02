package dev.my.first.project.samples.ar.arcore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {

    private ModelAnimator animator;
    private HashMap<Integer, Node> activeARNodes = new HashMap<>();

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        // Load the animated model
        this.loadModel(R.raw.andy_dance,
                modelRenderable -> {
                    // Add a listener to the OnFrame loop
                    Scene scene = arFragment.getArSceneView().getScene();
                    // The '0' index is associated with the first loaded AR image
                    scene.addOnUpdateListener(frameTime -> this.onUpdateFrame(arFragment, 0, modelRenderable));
                },
                throwable -> {
                    Toast toast = Toast.makeText(this, "Unable to load the model.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

        // Load the transformable model
        this.loadModel(R.raw.andy,
                modelRenderable -> {
                    // Define a behaviour associated with the model for the AR plane tap event
                    this.setOnTapArPlaneModelListener(arFragment, modelRenderable);
                },
                throwable -> {
                    Toast toast = Toast.makeText(this, "Unable to load the model.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });

    }

    private void loadModel(int resource, Consumer<ModelRenderable> onSuccess, Function<Throwable, Void> onError) {
        ModelRenderable.builder()
                .setSource(this, resource)
                .build()
                .thenAccept(onSuccess)
                .exceptionally(onError);
    }

    private void setOnTapArPlaneModelListener(ArFragment arFragment, ModelRenderable modelRenderable) {
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    // Create the anchor
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());
                    // Create the transformable node and add it to the anchor
                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(modelRenderable);
                    transformableNode.select();
                });
    }

    private void onUpdateFrame(ArFragment arFragment, int index, ModelRenderable modelRenderable) {
        Scene scene = arFragment.getArSceneView().getScene();
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame == null) return;
        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        // Check the tracker status for each detected image
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            // Get the detected AR image index to compare with the target one
            int augmentedImageIndex = augmentedImage.getIndex();
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    break;
                case TRACKING:
                    // Put the model inside the scene if it's not present
                    if (augmentedImageIndex == index && !activeARNodes.containsKey(index)) {
                        // Create the anchor
                        Anchor anchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(scene);
                        // Create the node and add it to the anchor
                        Node node = new Node();
                        node.setParent(anchorNode);
                        node.setRenderable(modelRenderable);
                        node.setLocalScale(node.getLocalScale().scaled(0.1f));
                        node.setOnTouchListener((hitTestResult, motionEvent) -> {
                            // Restart the '0' (first) animation on touch
                            this.startModelAnimation(modelRenderable, 0);
                            return true;
                        });
                        // Start the '0' (first) animation
                        this.startModelAnimation(modelRenderable, 0);
                        activeARNodes.put(index, node);
                    }
                    break;
                case STOPPED:
                    // Remove the model from the scene when the tracking ends
                    if (augmentedImageIndex == index && activeARNodes.containsKey(index)) {
                        Node node = activeARNodes.get(index);
                        scene.removeChild(node);
                        activeARNodes.remove(index);
                    }
                    break;
            }
        }
    }

    private void startModelAnimation(ModelRenderable modelRenderable, int index) {
        AnimationData data = modelRenderable.getAnimationData(index);
        if(this.animator!=null && this.animator.isRunning()) return;
        this.animator = new ModelAnimator(data, modelRenderable);
        animator.start();
    }

}
