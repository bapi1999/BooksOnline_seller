package com.sbdevs.booksonlineseller.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.*
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityOrderDetailsBinding
import com.sbdevs.booksonlineseller.databinding.ArOrderDetailsLay4Binding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.otherclass.TimeDateAgo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var viewProductBtn: Button

    val visible= View.VISIBLE
    val gone = View.GONE

    private val STORAGE_CODE = 1001

    private val loadingDialog = LoadingDialog()

    private lateinit var buyerId:String
    private lateinit var imageUrl:String
    private lateinit var productName: String

    private lateinit var orderId: String
    lateinit var lay4: ArOrderDetailsLay4Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderId = intent.getStringExtra("orderId").toString().trim()
        viewProductBtn = binding.lay1.viewProductBtn
        loadingDialog.show(supportFragmentManager,"show")

        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                getOrderDetails(orderId)
            }

        }
        lay4 = binding.lay4



    }


    override fun onStart() {
        super.onStart()

//        binding.lay1.viewProductBtn.setOnClickListener {
//
//        }

        binding.acceptOrderBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"Show")

            lay4.acceptImageButton.backgroundTintList = AppCompatResources
                .getColorStateList(this@OrderDetailsActivity,R.color.successGreen)
            lay4.acceptImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)

            updateOrder(orderId,"accepted")
            binding.statusTxt.text = "accepted"

            binding.acceptButtonContainer.visibility = gone
            binding.buttonContainer2.visibility = visible
            binding.packedBtn.visibility = visible
            binding.shippedBtn.visibility =gone

        }

        binding.rejectOrderBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"Show")

            updateOrder(orderId,"rejected")
            binding.statusTxt.text = "rejected"

        }


        binding.packedBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"Show")

            lay4.packedImageButton.backgroundTintList = AppCompatResources
                .getColorStateList(this@OrderDetailsActivity,R.color.blueLink)
            lay4.packedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)

            updateOrder(orderId,"packed")

            binding.statusTxt.text = "packed"
            binding.acceptButtonContainer.visibility = gone
            binding.buttonContainer2.visibility = visible
            binding.packedBtn.visibility = gone
            binding.shippedBtn.visibility =visible


        }


        binding.shippedBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"Show")

            lay4.shippedImageButton.backgroundTintList = AppCompatResources
                .getColorStateList(this@OrderDetailsActivity,R.color.indigo_500)
            lay4.shippedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)

            //send notification may produce runtime exception
            binding.acceptButtonContainer.visibility = gone
            binding.buttonContainer2.visibility = gone
            binding.statusTxt.text = "shipped"

            updateOrder(orderId,"shipped")
            sendNotification(buyerId,productName,imageUrl,"Shipped")
        }

        binding.cancelOrderBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"Show")
            cancelOrder(orderId)

            binding.statusTxt.text = "canceled"

            binding.acceptButtonContainer.visibility = gone
            binding.buttonContainer2.visibility = gone
            binding.orderCancelText.visibility = visible
        }

        binding.lay3.billingInvoiceBtn.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions,STORAGE_CODE)
            }else{
                billingInvoiceTable()
            }

        }

    }


    private fun getOrderDetails(orderId:String)  = CoroutineScope(Dispatchers.IO).launch{

        firebaseFirestore.collection("ORDERS")
            .document(orderId)
            .get().addOnSuccessListener {

                val orderTime: Date = it.getTimestamp("Time_ordered")!!.toDate()
                imageUrl =  it.getString("productThumbnail").toString()
                productName =  it.getString("productTitle").toString()
                val status =  it.getString("status").toString()
                val orderedQty =  it.getLong("ordered_Qty")

                val unitSellingPrice =  it.getLong("PRICE_SELLING_UNIT")
                val totalSellingPrice =  it.getLong("PRICE_SELLING_TOTAL")
                val deliveryCharge =  it.getLong("PRICE_SHIPPING_CHARGE")
                val totalPrice =  it.getLong("PRICE_TOTAL")

                buyerId =  it.getString("ID_Of_BUYER").toString()
                val tracKingId =  it.getString("ID_Of_Tracking")
                val orderId =  it.getString("ID_Of_ORDER")
                val sellerId =  it.getString("ID_Of_SELLER")
                val productId =  it.getString("productId")

                val already_paid:Boolean = it.getBoolean("already_paid")!!
                val isOrderCanceled = it.getBoolean("is_order_canceled")!!
                val orderCanceledBy = it.get("order_canceled_by").toString()
                val cancelletionReason = it.get("cancellation_reason").toString()

                val acceptedTime= it.getTimestamp("Time_accepted")
                val packedTime= it.getTimestamp("Time_packed")
                val shippedTime= it.getTimestamp("Time_shipped")
                val deliveredTime= it.getTimestamp("Time_delivered")
                val returnedTime= it.getTimestamp("Time_returned")
                val canceledTime= it.getTimestamp("Time_canceled")
                //val acceptTime: Date = it.getTimestamp("Time_accepted")!!.toDate()

                binding.orderIdTxt.text = orderId
                binding.trackingIdTxt.text = tracKingId


                binding.lay1.titleTxt.text = productName
                binding.lay1.priceTxt.text = totalPrice.toString()
                binding.lay1.productQuantity.text = orderedQty.toString()
                Glide.with(this@OrderDetailsActivity)
                    .load(imageUrl)
                    .placeholder(R.drawable.as_square_placeholder)
                    .into(binding.lay1.productImage)

                val timeAgo:String = TimeDateAgo().msToTimeAgo(this@OrderDetailsActivity,orderTime)
                binding.orderedTimeText.text = timeAgo







                val address:MutableMap<String,Any> = it.get("address") as MutableMap<String,Any>
                //val address:MutableMap<String,Any> = it.get("address") as MutableMap<String,Any>

                if (address.isEmpty()){

                    binding.orderWrongText.visibility = visible
                    binding.orderAddressContainer.visibility = gone


                }

                when(status){
                    "new" ->{
                        binding.acceptButtonContainer.visibility = visible
                        binding.buttonContainer2.visibility = gone

                        orderNew(orderTime)

                    }
                    "accepted" ->{


                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = visible
                        binding.packedBtn.visibility = visible
                        binding.shippedBtn.visibility =gone
                        binding.downloadsLabelContainer.visibility = visible
                        //
                        val acceptedT= acceptedTime!!.toDate()
                        orderNew(orderTime)
                        orderAccepted(acceptedT)



                    }
                    "packed" ->{

                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = visible
                        binding.packedBtn.visibility = gone
                        binding.shippedBtn.visibility =visible

                        val acceptedT= acceptedTime!!.toDate()
                        val packT = packedTime!!.toDate()
                        orderNew(orderTime)
                        orderAccepted(acceptedT)
                        orderPacked(packT)



                    }

                    "shipped"->{
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone

                        val acceptedT= acceptedTime!!.toDate()
                        val packT = packedTime!!.toDate()
                        val shippedT = shippedTime!!.toDate()
                        orderNew(orderTime)
                        orderAccepted(acceptedT)
                        orderPacked(packT)
                        orderShipped(shippedT)

                    }
                    "delivered"->{

                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone

                        val acceptT= acceptedTime!!.toDate()
                        val packT = packedTime!!.toDate()
                        val shipT = shippedTime!!.toDate()
                        val deliverT = deliveredTime!!.toDate()
                        orderNew(orderTime)
                        orderAccepted(acceptT)
                        orderPacked(packT)
                        orderShipped(shipT)
                        orderDelivered(deliverT)


                    }
                    "returned"->{
                        val returnTime: Date = it.getTimestamp("Time_returned")!!.toDate()
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone
                    }
//                    "canceled"->{
//                        val returnTime: Date = it.getTimestamp("Time_canceled")!!.toDate()
//                        binding.acceptButtonContainer.visibility = gone
//                        binding.buttonContainer2.visibility = gone
//                        binding.orderCancelText.visibility = visible
//
//                    }
                    else ->{
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone
                    }
                }

                if (isOrderCanceled){
                    val cancelT= canceledTime!!.toDate()

                    binding.cancelOrderBtn.visibility = gone

                    binding.orderTrackContainer.visibility = gone
                    binding.cancelContainer.visibility = visible
                    orderCanceled(cancelT,orderCanceledBy,cancelletionReason)

                    binding.statusTxt.text = "Canceled"

                }else{
                    binding.statusTxt.text = status
                }




                binding.lay1.viewProductBtn.setOnClickListener {
                    val productIntent = Intent(this@OrderDetailsActivity,ProductActivity::class.java)
                    productIntent.putExtra("productId",productId)
                    startActivity(productIntent)
                }

                loadingDialog.dismiss()

            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("Load Order details","${it.message}")
            }.await()
    }

    private fun orderNew(orderTime:Date){

        lay4.orderDate.text = getDateTime(orderTime)

        lay4.orderImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity,R.color.amber_600)
        lay4.orderImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }

    private fun orderAccepted(acceptTime:Date){
        lay4.acceptDate.text = getDateTime(acceptTime)

        lay4.acceptImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity,R.color.successGreen)
        lay4.acceptImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }
    private fun orderPacked(packedTime:Date){
        binding.lay4.packedDate.text = getDateTime(packedTime)
        binding.lay4.packedImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity,R.color.blueLink)
        binding.lay4.packedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }
    private fun orderShipped(shippedTime:Date){
        lay4.shippedDate.text = getDateTime(shippedTime)
        lay4.shippedImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity,R.color.indigo_500)
        lay4.shippedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }
    private fun orderDelivered(deliveredTime:Date){

        lay4.deliveredDate.text = getDateTime(deliveredTime)
        lay4.deliveredImageButton.backgroundTintList = AppCompatResources
            .getColorStateList(this@OrderDetailsActivity,R.color.ratingGreen)
        lay4.deliveredImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
    }
    private fun orderReturned(){

    }

    private fun orderCanceled(deliveredTime:Date,orderCanceledBy:String,reason:String){

        binding.lay0.cancellationTime.text = getDateTime(deliveredTime)
        binding.lay0.cancellationText.text = "Order is canceled by $orderCanceledBy"
        binding.lay0.cancellationReason.text = "Reason: $reason"
    }



    private fun updateOrder(orderId: String, status:String){

        val orderMap:MutableMap<String,Any> = HashMap()
        orderMap["status"] = status

        orderMap["Time_$status"] = FieldValue.serverTimestamp()
        firebaseFirestore.collection("ORDERS")
            .document(orderId).update(orderMap)
            .addOnSuccessListener {
                Log.i("$status order","successful")
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("$status order","${it.message}")
            }

    }

    private fun cancelOrder(orderId: String){

        val orderMap:MutableMap<String,Any> = HashMap()
        orderMap["status"] = "canceled"
        orderMap["is_order_canceled"] = true
        orderMap["order_canceled_by"] = "seller"
        orderMap["cancellation_reason"] = "reason"
        orderMap["Time_canceled"] = FieldValue.serverTimestamp()

        orderMap["Time_canceled"] = FieldValue.serverTimestamp()
        firebaseFirestore.collection("ORDERS")
            .document(orderId).update(orderMap)
            .addOnSuccessListener {
                Log.i("canceled order","successful")
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("canceled order","${it.message}")
            }


        val cancelT= Date()
        binding.cancelOrderBtn.visibility = gone
        binding.orderTrackContainer.visibility = gone
        binding.statusTxt.text = "Canceled"

        binding.lay0.cancellationTime.text = TimeDateAgo().msToTimeAgo(this,cancelT)
        binding.lay0.cancellationText.text = "Order is canceled by seller"


    }



    private fun sendNotification(buyerId:String,productName:String,url:String,status: String){
        val ref = firebaseFirestore.collection("USERS").document(buyerId).collection("USER_DATA")
            .document("MY_NOTIFICATION").collection("NOTIFICATION")

        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] = "$status:$productName"
        notificationMap["image"] = url
        notificationMap["order_id"] = orderId
        notificationMap["seller_id"] = user!!.uid
        notificationMap["seen"] = false
//

        ref.add(notificationMap)
            .addOnSuccessListener {

        }.addOnFailureListener {
            Log.e("get buyer notification","${it.message}")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(date: Date): String? {
        return try {
            val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a")
            //val netDate = Date(s.toLong() * 1000)
            sdf.format(date)
        } catch (e: Exception) {
            e.toString()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            STORAGE_CODE->{
                if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    billingInvoiceTable()
                }else{
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





    private fun billingInvoiceTable(){
        val pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        val file:File = File(pdfPath,"invoice.pdf")
        //val outPutStream:OutputStream = FileOutputStream(file)
        val writer = PdfWriter(file)
        val pdfDocument: PdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        val logo = AppCompatResources.getDrawable(this,R.drawable.books_online_seller_logo)
        val bitmap  = logo?.toBitmap()
        val stream2 = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream2)
        }

        val bitmapByteArr:ByteArray = stream2.toByteArray()
        val imagedata:ImageData = ImageDataFactory.create(bitmapByteArr)
        val image = Image(imagedata)
        image.setHeight(40F)


        val columnWidth1 = floatArrayOf(200F, 180F, 180F)
        val table1 = Table(columnWidth1)

        //TABLE 1 -- 1
        table1.addCell(Cell().add(Paragraph("INVOICE").setFontSize(15F).setBold()))
        table1.addCell(Cell().add(Paragraph("ORDERED THROUGH: \n BOOKS ONLINE").setFontSize(10F)))
        table1.addCell(Cell().add(image))
        //TABLE 1 -- 2
        table1.addCell(Cell().add(Paragraph("Invoice No \n FF3993H3490").setFontSize(6F)))
        table1.addCell(Cell().add(Paragraph("SERIAL NO \n 1323003993SL").setFontSize(6F)))
        table1.addCell(Cell().add(Paragraph("")))
        //TABLE 1 -- 2
        val textSoldBy:Text = Text("SOLD BY: ")
        textSoldBy.apply {
            setBold()
            setFontSize(10F)
            //setFontColor(DeviceRgb(18,192,33))
        }
        val paragraphSoldBy = Paragraph()
        paragraphSoldBy.apply {
            setFontSize(10F)
            add(textSoldBy)
            add("name,\n"+"address_1, "+"address2, \n"+"pincode, "+"state")
        }
        table1.addCell(Cell(1,2).add(paragraphSoldBy))
        //table1.addCell(Cell().add(Paragraph("")))
        table1.addCell(Cell().add(Paragraph("")))


        val paragraphBlank= Paragraph()
        paragraphBlank.add("")


        val columnWidth2 = floatArrayOf(280F, 280F)
        val table2 = Table(columnWidth2)
        val textBilledTo:Text = Text("SHIPPING ADDRESS\n")

        textBilledTo.apply {
            setBold()
            setFontSize(12F)
            //setFontColor(DeviceRgb(18,192,33))
        }

        val textBilledName:Text = Text("JHONE DOE\n")
        textBilledName.setBold().setFontSize(10F)

        val paragraphBilledTo = Paragraph()
        paragraphBilledTo.apply {
            add(textBilledTo)
            add(textBilledName)
            add("address_1, "+"address2, \n"+"pincode,"+"state")
            setFontSize(10F)
        }
        table2.addCell(Cell(1,2).add(paragraphBilledTo))

        val columnWidth3 = floatArrayOf(360F, 40F,80F,80F)
        val table3 = Table(columnWidth3)

        //Table 3 -- 1
        table3.addCell(Cell().add(Paragraph("PRODUCTS").setBold().setFontSize(10F)))
        table3.addCell(Cell().add(Paragraph("QTY").setFontSize(10F).setBold()))
        table3.addCell(Cell().add(Paragraph("UNIT PRICE").setFontSize(10F).setBold()))
        table3.addCell(Cell().add(Paragraph("PRICE").setFontSize(10F).setBold()))

        //Table 3 -- 2 font size 8
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))

        //Table 3 -- 3 font size 8
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        //Table 3 -- 4 font size 8
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        //Table 3 -- 5 font size 8
        table3.addCell(Cell().add(Paragraph("Discount").setFontSize(8F).setBold()))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("-100").setFontSize(8F)))
        //Table 3 -- 6 font size 8
        table3.addCell(Cell().add(Paragraph("Shipping charge").setFontSize(8F).setBold()))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("70").setFontSize(8F)))

        //Table 3 -- 7 font size 8
        table3.addCell(Cell().add(Paragraph("Total").setFontSize(10F).setBold()))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))
        table3.addCell(Cell().add(Paragraph("")))


        document.add(table1)
        document.add(paragraphBlank)
        document.add(table2)
        document.add(paragraphBlank)
        document.add(table3)
        document.add(paragraphBlank)
        document.close()
        Toast.makeText(this,"pdf crated",Toast.LENGTH_SHORT).show()

    }

    private fun creatTable(){
        val pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        val file:File = File(pdfPath,"invoice.pdf")
        //val outPutStream:OutputStream = FileOutputStream(file)
        val writer = PdfWriter(file)
        val pdfDocument: PdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        val columnWidth = floatArrayOf(190F, 140F, 112F, 112F,112F)
        val table = Table(columnWidth)

        //row -- 1
        table.addCell(Cell(1,2).add(Paragraph("Deal bill").setFontSize(20F).setBold()).setBorder(Border.NO_BORDER))
//        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))

        //row -- 2
        table.addCell(Cell().add(Paragraph(" 123 street \n"+"address 1\n"+"address_2"+"pincode"+"state")))
        table.addCell(Cell().add(Paragraph("deal bill pvt ltd\n"+"Ph.no 12334343")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))

        //row -- 3
        table.addCell(Cell().add(Paragraph("\n")).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))
        table.addCell(Cell().add(Paragraph("")).setBorder(Border.NO_BORDER))

        //row -- 4
        val textBilledTo:Text = Text("Billed To:\n")
        textBilledTo.apply {
            setBold()
            setFontColor(DeviceRgb(18,192,33))
        }
        val paragraphBilledTo = Paragraph()
        paragraphBilledTo.apply {
            add(textBilledTo)
            add("name\n"+"address_1"+"address2\n"+"pincode\n"+"state")
        }
        table.addCell(Cell().add(paragraphBilledTo))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))


        //row -- 5
        table.addCell(Cell(2,1).add(Paragraph("INVOICE")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))


        //row -- 6
//        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("Description")))
        table.addCell(Cell().add(Paragraph("Unit cost")))
        table.addCell(Cell().add(Paragraph("QTY")))
        table.addCell(Cell().add(Paragraph("AMOUNT")))


        //row -- 7
        table.addCell(Cell(2,1).add(Paragraph("INVOICE NUMBRE :\n"+"010101")))
        table.addCell(Cell().add(Paragraph("ITEM 1")))
        table.addCell(Cell().add(Paragraph("100")))
        table.addCell(Cell().add(Paragraph("2")))
        table.addCell(Cell().add(Paragraph("200")))


        //row -- 8
//        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("ITEM 2")))
        table.addCell(Cell().add(Paragraph("100")))
        table.addCell(Cell().add(Paragraph("3")))
        table.addCell(Cell().add(Paragraph("300")))


        //row -- 9
        table.addCell(Cell(2,1).add(Paragraph("DATE\n"+"12/23/56")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))


        //row -- 10
//        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))


        //row -- 11
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("SUBTOTAL")))
        table.addCell(Cell().add(Paragraph("500")))


        //row -- 12
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("DISCOUNT")))
        table.addCell(Cell().add(Paragraph("100")))


        //row -- 13
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("TAX RATE")))
        table.addCell(Cell().add(Paragraph("10")))


        //row -- 14
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("TAX")))
        table.addCell(Cell().add(Paragraph("400")))


        //row -- 15
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell(1,2).add(Paragraph("=======")))
//        table.addCell(Cell().add(Paragraph("")))


        //row -- 16
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell(1,2).add(Paragraph("INVOICE TOTAL\n"+"440")))
//        table.addCell(Cell().add(Paragraph("")))

        //row -- 17
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))


        //row -- 18
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))
        table.addCell(Cell().add(Paragraph("")))






        document.add(table)
        document.close()
        Toast.makeText(this,"pdf crated",Toast.LENGTH_SHORT).show()
    }




}