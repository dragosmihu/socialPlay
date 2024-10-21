package com.example.socialplay;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectTenantActivity extends AppCompatActivity {

    private RecyclerView tenantRecyclerView;
    private TenantAdapter tenantAdapter;
    private List<Tenant> tenantList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tenant);

        tenantRecyclerView = findViewById(R.id.tenant_recycler_view);
        tenantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        tenantList = new ArrayList<>();
        tenantAdapter = new TenantAdapter(tenantList);
        tenantRecyclerView.setAdapter(tenantAdapter);

        db = FirebaseFirestore.getInstance();

        fetchTenants();
    }

    private void fetchTenants() {
        db.collection("tenants").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("SelectTenantActivity", "Listen failed.", error);
                    return;
                }

                tenantList.clear();
                for (DocumentSnapshot doc : value) {
                    Tenant tenant = doc.toObject(Tenant.class);
                    tenantList.add(tenant);
                }
                tenantAdapter.notifyDataSetChanged();
            }
        });
    }
}
