package com.financetracker.ui.merchants;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.financetracker.data.db.entity.Merchant;
import com.financetracker.data.repository.MerchantRepository;
import java.util.List;

public class MerchantsViewModel extends AndroidViewModel {

    private final MerchantRepository merchantRepository;
    public final LiveData<List<Merchant>> allMerchants;

    public MerchantsViewModel(Application application) {
        super(application);
        merchantRepository = new MerchantRepository(application);
        allMerchants = merchantRepository.getAllActive();
    }

    public void updateMerchant(Merchant merchant) {
        merchant.updatedAt = System.currentTimeMillis();
        merchantRepository.update(merchant, null);
    }

    public void deleteMerchant(String uuid) {
        merchantRepository.delete(uuid, null);
    }
}



