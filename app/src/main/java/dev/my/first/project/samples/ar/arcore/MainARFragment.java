package dev.my.first.project.samples.ar.arcore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;

public class MainARFragment extends ArFragment {

    private static final String AR_IMAGE = "default.jpg";
    private static final String SAMPLE_IMAGE_DATABASE = "sample_database.imgdb";
    private int arImageIndex;

    public int getARImageIndex() {
        return this.arImageIndex;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        // Turn off the plane discovery since we're only looking for images
        /*
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        */
        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);
        this.arImageIndex = setupAugmentedImageDatabase(config, session);
        return config;
    }

    private int setupAugmentedImageDatabase(Config config, Session session) {
        int index = -1;
        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(session);
        try (InputStream is = getContext().getAssets().open(SAMPLE_IMAGE_DATABASE)) {
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
        } catch (IOException e) {
            System.out.println(e);
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return index;
    }

}
