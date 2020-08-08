package com.scriptsbundle.nokri.candidate.edit.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.scriptsbundle.nokri.manager.Nokri_DialogManager;
import com.scriptsbundle.nokri.manager.Nokri_RequestHeaderManager;
import com.scriptsbundle.nokri.manager.Nokri_SharedPrefManager;
import com.scriptsbundle.nokri.manager.Nokri_ToastManager;
import com.scriptsbundle.nokri.network.Nokri_ServiceGenerator;
import com.scriptsbundle.nokri.rest.RestService;
import com.scriptsbundle.nokri.R;
import com.scriptsbundle.nokri.candidate.edit.adapters.Nokri_FilePreviewAdapter;
import com.scriptsbundle.nokri.candidate.edit.models.Nokri_FileModel;
import com.scriptsbundle.nokri.custom.ProgressRequestBody;
import com.scriptsbundle.nokri.manager.Nokri_FontManager;
import com.scriptsbundle.nokri.manager.Nokri_GoogleAnalyticsManager;
import com.scriptsbundle.nokri.manager.Nokri_PopupManager;
import com.scriptsbundle.nokri.manager.Nokri_UploadProgressDialolque;
import com.scriptsbundle.nokri.utils.Nokri_Utils;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Nokri_AddPortfolioFragment extends Fragment implements View.OnClickListener ,Nokri_FilePreviewAdapter.OnItemClickListener,Nokri_PopupManager.ConfirmInterface, ProgressRequestBody.UploadCallbacks {
    private TextView addPortfolioTextView,portfolioFormatAllowedTextView,dropFilesTextView;

    private Nokri_FontManager fontManager;
    private RelativeLayout uploadContainer;
    private ArrayList<String> paths =new ArrayList<>() ;
    private ArrayList<NormalFile>imagePaths;
    private ArrayList<Nokri_FileModel>modelList;
    private Nokri_PopupManager popupManager;
    private Nokri_FilePreviewAdapter adapter;
    private RecyclerView recyclerView;
    private String id;
    private Nokri_UploadProgressDialolque progressDialolque;
    private int currentFileNumber = 1;
    private int totalFiles = 1;
    private Nokri_DialogManager dialogManager;
    private Button saveYoutubeButton;
    private TextView youtubeTextView;
    private EditText youtubeEditText;

    @Override
    public void onResume() {
        super.onResume();
        Nokri_GoogleAnalyticsManager.getInstance().trackScreenView(getClass().getSimpleName());
    }

    public Nokri_AddPortfolioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nokri_add_portfolio, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nokri_initialize();
        nokri_setFonts();
        nokri_getPortfolio();
    }

    private void nokri_setFonts() {
        fontManager.nokri_setMonesrratSemiBioldFont(addPortfolioTextView,getActivity().getAssets());
        fontManager.nokri_setMonesrratSemiBioldFont(youtubeTextView,getActivity().getAssets());
        fontManager.nokri_setMonesrratSemiBioldFont(portfolioFormatAllowedTextView,getActivity().getAssets());
        fontManager.nokri_setOpenSenseFontTextView(dropFilesTextView,getActivity().getAssets());
        fontManager.nokri_setOpenSenseFontButton(saveYoutubeButton,getActivity().getAssets());

    }

    private void nokri_initialize() {
        fontManager = new Nokri_FontManager();
        saveYoutubeButton = getView().findViewById(R.id.btn_saveyoutube);
        addPortfolioTextView = getView().findViewById(R.id.txt_add_portfolio);
        portfolioFormatAllowedTextView = getView().findViewById(R.id.txt_portfolio_format_allowed);
        dropFilesTextView = getView().findViewById(R.id.txt_drop_files);
        Nokri_Utils.setEditBorderButton(getContext(),saveYoutubeButton);
        uploadContainer = getView().findViewById(R.id.upload_container);
        uploadContainer.setOnClickListener(this);
        recyclerView = getView().findViewById(R.id.recyclerview);
        popupManager = new Nokri_PopupManager(getContext(),this);

        youtubeEditText = getView().findViewById(R.id.edittxt_youtube);
        youtubeTextView = getView().findViewById(R.id.txt_youtube);
        saveYoutubeButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.upload_container: {
                Intent intent4 = new Intent(getContext(), NormalFilePickActivity.class);
                intent4.putExtra(Constant.MAX_NUMBER, 9);
                intent4.putExtra(NormalFilePickActivity.SUFFIX, new String[]{"xlsx", "xls", "doc", "docx", "ppt", "pptx", "pdf","txt"});
                startActivityForResult(intent4, Constant.REQUEST_CODE_PICK_FILE);
            }
                break;
            case R.id.btn_saveyoutube:
                nokri_postYoutubeUrl(youtubeEditText.getText().toString());
                break;
        }
    }


    private void nokri_uploadPortfolioRequest(final ArrayList<String> paths){
        progressDialolque = new Nokri_UploadProgressDialolque(getContext());
        progressDialolque.showUploadDialogue();
        final List<MultipartBody.Part> parts = new ArrayList<>();

 //       Nokri_DialogManager.showAlertDialogWithCustomText(getContext(),"Uploading Portfolio");
          totalFiles = paths.size();
            for(int i = 0;i<paths.size();i++){

                File file = new File(paths.get(i));

                //RequestBody requestBody  = RequestBody.create(MediaType.parse("*/*"), file);
                ProgressRequestBody requestBody = new ProgressRequestBody(file,this);
                 parts.add(MultipartBody.Part.createFormData("portfolio_upload[]", file.getName(), requestBody));

                Log.v("uploaddddd",requestBody.toString());
            }



        RestService restService =  Nokri_ServiceGenerator.createServiceNoTimeout(RestService.class, Nokri_SharedPrefManager.getEmail(getContext()), Nokri_SharedPrefManager.getPassword(getContext()),getContext());

        final Call<ResponseBody> myCall;
        if(Nokri_SharedPrefManager.isSocialLogin(getContext())) {
            myCall = restService.postUploadPortfolio(parts, Nokri_RequestHeaderManager.UploadImageAddSocial());
        } else

        {
            myCall = restService.postUploadPortfolio(parts, Nokri_RequestHeaderManager.UploadImageAddHeaders());
        }
        progressDialolque.setCloseClickListener(new Nokri_UploadProgressDialolque.CloseClickListener() {
            @Override
            public void onCloseClick() {
                myCall.cancel();
            }
        });
        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObject) {

                if(responseObject.isSuccessful()){

                    try {
                        //Log.v("Uploadddddddddddddddddd",responseObject.body().string());
                        JSONObject jsonObject = new JSONObject(responseObject.body().string());

                        if(jsonObject.getBoolean("success")){
                        Nokri_ToastManager.showLongToast(getContext(),jsonObject.getString("message"));


                         // JSONArray dataArray = jsonObject.getJSONArray("data");

                            progressDialolque.handleSuccessScenerion();
                            currentFileNumber = 1;
                            Log.v("uploaddddd",jsonObject.toString());


                            nokri_getPortfolio();

                        }
                        else{
                            progressDialolque.handleFailedScenerio();
                            currentFileNumber = 1;
                            Nokri_ToastManager.showLongToast(getContext(),jsonObject.getString("message"));
                            Log.v("uploaddddd",jsonObject.toString());
                        }

                    } catch (JSONException e) {

                      /*  Nokri_DialogManager.showCustom(e.getMessage());
                        Nokri_DialogManager.hideAfterDelay();*/
                        progressDialolque.handleFailedScenerio();
                        currentFileNumber = 1;
                        Log.v("uploaddddd",e.getMessage()+" JSONException");
                        e.printStackTrace();
                    } catch (IOException e) {
                      /*  Nokri_DialogManager.showCustom(e.getMessage());
                        Nokri_DialogManager.hideAfterDelay();*/
                        e.printStackTrace();
                        progressDialolque.handleFailedScenerio();
                        currentFileNumber = 1;
                        Log.v("uploaddddd",e.getMessage()+" IOException");
                    }
                }
                else{
                    progressDialolque.handleFailedScenerio();
                    currentFileNumber = 1;
                    Log.v("uploaddddd",responseObject.message().toString()+" IOException");
                }
               // Nokri_DialogManager.hideAlertDialog();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
               /* Nokri_DialogManager.showCustom(t.getMessage());
                Nokri_DialogManager.hideAfterDelay();*/
                Log.v("uploaddddd",t.getMessage() +" OnFailure");
                progressDialolque.handleFailedScenerio();
                currentFileNumber = 1;
                Nokri_ToastManager.showLongToast(getContext(),t.getMessage()); }
        });
    }

    private void nokri_setupRecyclerview(){


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new Nokri_FilePreviewAdapter(modelList,getContext(),this,1);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        dialogManager.hideAfterDelay(6000);
    }
    private void nokri_getPortfolio(){
        dialogManager = new Nokri_DialogManager();
        dialogManager.showAlertDialog(getActivity());
        RestService restService =  Nokri_ServiceGenerator.createService(RestService.class, Nokri_SharedPrefManager.getEmail(getContext()), Nokri_SharedPrefManager.getPassword(getContext()),getContext());

        Call<ResponseBody> myCall;
        if(Nokri_SharedPrefManager.isSocialLogin(getContext())) {
            myCall = restService.getCandidatePortfolio(Nokri_RequestHeaderManager.addSocialHeaders());
        } else

        {
            myCall = restService.getCandidatePortfolio( Nokri_RequestHeaderManager.addHeaders());
        }
      //  Call<ResponseBody> myCall = service.getCandidatePortfolio(Nokri_RequestHeaderManager.addHeaders());
        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObject) {
                if(responseObject.isSuccessful()){
                    try {
                        String buttonText = null;
                        modelList = new ArrayList<>();
                        //    Log.v("response",responseObject.body().string());
                        JSONObject jsonObject = new JSONObject(responseObject.body().string());
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        JSONArray dataArray = dataObject.getJSONArray("img");
                        JSONArray extrasArray = dataObject.getJSONArray("extra");
                        for(int i = 0;i<extrasArray.length();i++){

                            JSONObject extra = extrasArray.getJSONObject(i);
                            if(extra.getString("field_type_name").equals("section_name")){
                                addPortfolioTextView.setText(extra.getString("value")+":");
                            }
                            else
                            if(extra.getString("field_type_name").equals("section_txt")){
                                portfolioFormatAllowedTextView.setText(extra.getString("value"));
                            }
                            else
                            if(extra.getString("field_type_name").equals("click_text")){
                                if(dataArray.length()==0) {
                                    dropFilesTextView.setText(extra.getString("value"));
                                    dropFilesTextView.setVisibility(View.VISIBLE);
                                }else dropFilesTextView.setVisibility(View.GONE);
                            }
                           else if(extra.getString("field_type_name").equals("video_save_btn")){
                                saveYoutubeButton.setText(extra.getString("value"));
                            }
                            else
                            if(extra.getString("field_type_name").equals("del_txt")){
                               buttonText = extra.getString("value");
                            }
                            else
                            if(extra.getString("field_type_name").equals("video_url")){
                                youtubeEditText.setText(extra.getString("value"));
                                youtubeTextView.setText(extra.getString("key"));

                            }

                            else
                            if(extra.getString("field_type_name").equals("no_video_url")){
                                youtubeEditText.setHint(extra.getString("value"));


                            }
                        }


                        for(int i=0;i<dataArray.length();i++)
                        {   JSONObject response = dataArray.getJSONObject(i);

                            Nokri_FileModel model = new Nokri_FileModel();
                            model.setUrl(response.getString("value"));
                            model.setId(response.getString("fieldname"));
                            model.setButtonText(buttonText);
                            modelList.add(model);
                        }
                        nokri_setupRecyclerview();

                         }
                    catch (JSONException e) {
                        //  Nokri_DialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    } catch (IOException e) {

                        //  Nokri_DialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    }
                }
                else {
                    dialogManager.showCustom(responseObject.message());
                    dialogManager.hideAfterDelay();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Nokri_ToastManager.showLongToast(getContext(),t.getMessage());
                dialogManager.hideAfterDelay();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode)
//        {
//            case FilePickerConst.REQUEST_CODE_PHOTO:
//                if(resultCode== Activity.RESULT_OK && data!=null)
//                {
//                    imagePaths = new ArrayList<>();
//                    imagePaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
////                    Nokri_ToastManager.showLongToast(getContext(),imagePaths.get(0));
//
//
//                    nokri_uploadPortfolioRequest(imagePaths);
//                }
//                break;

        if (Constant.REQUEST_CODE_PICK_FILE==requestCode) {
            if (resultCode == RESULT_OK && data != null) {
                imagePaths = new ArrayList<>();
                imagePaths.addAll(data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE));
                ArrayList<String>paths = new ArrayList<>();
                for(NormalFile file : imagePaths){
                    paths.add(file.getPath());
                }
                nokri_uploadPortfolioRequest(paths);
            }

        }
    }

    @Override
    public void onConfirmClick(Dialog dialog) {
        dialog.dismiss();
        nokri_deletePortfolio();
    }

    @Override
    public void onItemClick(Nokri_FileModel item, int position) {
        id = item.getId();
        popupManager.nokri_showDeletePopup();
    }

    private void nokri_deletePortfolio() {

        dialogManager = new Nokri_DialogManager();
        dialogManager.showAlertDialog(getActivity());
        JsonObject params = new JsonObject();
        params.addProperty("portfolio_id",id);

        RestService restService =  Nokri_ServiceGenerator.createService(RestService.class, Nokri_SharedPrefManager.getEmail(getContext()), Nokri_SharedPrefManager.getPassword(getContext()),getContext());

        Call<ResponseBody> myCall;
        if(Nokri_SharedPrefManager.isSocialLogin(getContext())) {
            myCall = restService.postDeletePortfolio(params, Nokri_RequestHeaderManager.addSocialHeaders());
        } else

        {
            myCall = restService.postDeletePortfolio(params, Nokri_RequestHeaderManager.addHeaders());
        }
     //   Call<ResponseBody> myCall = restService.postDeletePortfolio(params,Nokri_RequestHeaderManager.addHeaders());

        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObject) {
                if(responseObject.isSuccessful()){

                    try {
                        JSONObject response = new JSONObject(responseObject.body().string());
                        if (response.getBoolean("success")) {
                            dialogManager.hideAlertDialog();
                            popupManager.nokri_showSuccessPopup(response.getString("message"));
                            nokri_getPortfolio();
                        } else {
                            dialogManager.showCustom(responseObject.message());

                            dialogManager.hideAfterDelay();
                        }

                    } catch (JSONException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    } catch (IOException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    }
                }

                else {
                    dialogManager.showCustom(responseObject.message());
                    dialogManager.hideAfterDelay();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Nokri_ToastManager.showLongToast(getContext(),t.getMessage());
                dialogManager.hideAfterDelay();
            }
        });
    }



    private void nokri_postYoutubeUrl(String url) {

        dialogManager = new Nokri_DialogManager();
        dialogManager.showAlertDialog(getActivity());
        JsonObject params = new JsonObject();
        params.addProperty("video_url",url);
        RestService restService =  Nokri_ServiceGenerator.createService(RestService.class, Nokri_SharedPrefManager.getEmail(getContext()), Nokri_SharedPrefManager.getPassword(getContext()),getContext());

        Call<ResponseBody> myCall;
        if(Nokri_SharedPrefManager.isSocialLogin(getContext())) {
            myCall = restService.postYoutbe(params, Nokri_RequestHeaderManager.addSocialHeaders());
        } else

        {
            myCall = restService.postYoutbe(params, Nokri_RequestHeaderManager.addHeaders());
        }
        //   Call<ResponseBody> myCall = restService.postDeletePortfolio(params,Nokri_RequestHeaderManager.addHeaders());

        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObject) {
                if(responseObject.isSuccessful()){

                    try {
                        JSONObject response = new JSONObject(responseObject.body().string());
                        if (response.getBoolean("success")) {
                            dialogManager.hideAlertDialog();
                            Nokri_ToastManager.showLongToast(getContext(),response.getString("message"));
                        } else {
                            Nokri_ToastManager.showLongToast(getContext(),response.getString("message"));

                            dialogManager.hideAfterDelay();
                        }

                    } catch (JSONException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    } catch (IOException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    }
                }

                else {
                    dialogManager.showCustom(responseObject.message());
                    dialogManager.hideAfterDelay();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Nokri_ToastManager.showLongToast(getContext(),t.getMessage());
                dialogManager.hideAfterDelay();
            }
        });
    }

    @Override
    public void onProgressUpdate(int percentage) {

        progressDialolque.updateProgress(percentage,currentFileNumber,totalFiles);



    }

    @Override
    public void onError() {
        progressDialolque.handleFailedScenerio();
    }

    @Override
    public void onFinish() {
           if(currentFileNumber<totalFiles)
            currentFileNumber = currentFileNumber+1;

    }
}
