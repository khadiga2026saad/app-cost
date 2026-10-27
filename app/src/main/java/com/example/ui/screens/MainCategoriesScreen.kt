package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private object CategoryPalette {
 val background=Color(0xFFF9F9FF); val white=Color.White; val border=Color(0xFFE2E8F0)
 val ink=Color(0xFF0F172A); val muted=Color(0xFF64748B); val blue=Color(0xFF0284C7)
 val gold=Color(0xFFD4AF37); val green=Color(0xFF10B981)
}
private data class CategoryTile(val id:String,val title:String,val subtitle:String,val icon:ImageVector)

@Composable
fun MainCategoriesScreen(
 viewModel: FinanceViewModel? = null,
 onNavigateBack:()->Unit = {},
 onCategoryClick:(String)->Unit = {},
 onAddNewCategory:()->Unit = {},
 onLogout:()->Unit = {}
) {
 val items=remember { listOf(
  CategoryTile("personal_expenses","المصروفات الشخصية","المصروفات الشخصية",Icons.Outlined.Person),
  CategoryTile("reports","التقارير","التقارير والتحليلات",Icons.Outlined.BarChart),
  CategoryTile("my_car","سيارتي","مصاريف السيارات",Icons.Outlined.DirectionsCar),
  CategoryTile("income_certificates","الدخل والشهادات","الدخل والشهادات البنكية",Icons.Outlined.AttachMoney),
  CategoryTile("home_expenses","مصروفات المنزل","كل مصروفات المنزل",Icons.Outlined.Home),
  CategoryTile("education","التعليم","مصاريف التعليم",Icons.Outlined.MenuBook)
 ) }
 val summary=viewModel?.currentMonthSummary?.collectAsStateWithLifecycle()?.value
 // No fabricated records or amounts: the reference mockup's example figures are deliberately omitted.
 CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
 Scaffold(containerColor=CategoryPalette.background,
  topBar={ CategoryHeader(onLogout) },
  bottomBar={
   NavigationBar(containerColor=CategoryPalette.white) {
    NavigationBarItem(selected=false,onClick=onNavigateBack,icon={Icon(Icons.Outlined.GridView,null)},label={Text("الرئيسية")})
    NavigationBarItem(selected=true,onClick={},icon={Icon(Icons.Outlined.ReceiptLong,null)},label={Text("التكاليف")})
    NavigationBarItem(selected=false,onClick={},enabled=false,icon={Icon(Icons.Default.AutoAwesome,null)},label={Text("المستشار الذكي")})
    NavigationBarItem(selected=false,onClick={onCategoryClick.let { { it("reports") } }},icon={Icon(Icons.Outlined.BarChart,null)},label={Text("التقارير")})
    NavigationBarItem(selected=false,onClick={},enabled=false,icon={Icon(Icons.Outlined.Person,null)},label={Text("الحساب")})
   }
  }
 ) { padding ->
  LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(13.dp)) {
   item { Spacer(Modifier.height(2.dp)); CategoryTicker() }
   item {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
     Text("الأفرع والأقسام الرئيسية",fontSize=17.sp,fontWeight=FontWeight.Bold,color=CategoryPalette.ink)
     Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFFF0F3FF)) {
      Text("٦ أقسام نشطة",Modifier.padding(horizontal=10.dp,vertical=5.dp),fontSize=11.sp,color=CategoryPalette.ink)
     }
    }
   }
   item {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
     items.chunked(2).forEach { pair ->
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
       pair.forEach { item ->
        CategoryTileCard(item,Modifier.weight(1f),onClick={onCategoryClick(item.id)})
       }
      }
     }
    }
   }
   item {
    Surface(onClick=onAddNewCategory,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=CategoryPalette.white,shadowElevation=2.dp,border=BorderStroke(1.dp,CategoryPalette.border)) {
     Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
      Icon(Icons.Default.Add,null,tint=CategoryPalette.ink)
      Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
       Text("إضافة قسم مالي جديد",fontWeight=FontWeight.Bold,fontSize=14.sp,color=CategoryPalette.ink)
       Text("تخصيص تصنيف، ميزانية، أو بطاقة مصروف",fontSize=10.sp,color=CategoryPalette.muted)
      }
      Icon(Icons.Default.ArrowBack,null,tint=CategoryPalette.muted)
     }
    }
    Spacer(Modifier.height(12.dp))
   }
  }
 }
 }
}
@Composable private fun CategoryTileCard(item:CategoryTile,modifier:Modifier=Modifier,onClick:()->Unit) {
 Surface(onClick=onClick,modifier=modifier.heightIn(min=139.dp),shape=RoundedCornerShape(18.dp),color=CategoryPalette.white,border=BorderStroke(1.dp,CategoryPalette.border),shadowElevation=1.dp) {
  Column(Modifier.fillMaxWidth().padding(12.dp)) {
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
    Surface(shape=RoundedCornerShape(10.dp),color=Color(0xFFE0F2FE)) {
     Icon(item.icon,item.title,Modifier.padding(9.dp).size(20.dp),tint=CategoryPalette.blue)
    }
    Text(if(item.id=="reports") "قيد الإعداد" else "—",fontSize=11.sp,fontWeight=FontWeight.Bold,color=CategoryPalette.ink)
   }
   Spacer(Modifier.height(12.dp))
   Text(item.title,fontSize=14.sp,fontWeight=FontWeight.Bold,color=CategoryPalette.ink,maxLines=2,overflow=TextOverflow.Ellipsis)
   Spacer(Modifier.height(3.dp))
   Text(item.subtitle,fontSize=10.sp,color=CategoryPalette.muted,maxLines=1,overflow=TextOverflow.Ellipsis)
   Spacer(Modifier.height(9.dp))
   Text("البيانات قيد الربط",fontSize=9.sp,color=CategoryPalette.muted)
  }
 }
}
@Composable private fun CategoryHeader(onLogout:()->Unit) {
 var now by remember { mutableStateOf(Date()) }
 var menu by remember { mutableStateOf(false) }
 LaunchedEffect(Unit) { while(true) { now=Date();delay(1000) } }
 Surface(color=CategoryPalette.white,shadowElevation=1.dp) {
  Row(Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=10.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
   Row(verticalAlignment=Alignment.CenterVertically) {
    Surface(shape=CircleShape,color=Color(0xFFE0F2FE)) { Icon(Icons.Default.Person,null,Modifier.padding(9.dp),tint=CategoryPalette.ink) }
    Spacer(Modifier.width(6.dp))
    Column { Text("مرحباً بك",fontSize=13.sp,fontWeight=FontWeight.Bold);Text("لوحة الإدارة المالية",fontSize=10.sp,color=CategoryPalette.blue) }
   }
   Column(horizontalAlignment=Alignment.CenterHorizontally) {
    Text(SimpleDateFormat("hh:mm:ss a",Locale("ar")).format(now),fontSize=10.sp,fontWeight=FontWeight.Bold)
    Text(SimpleDateFormat("d MMMM yyyy",Locale("ar")).format(now),fontSize=9.sp,color=CategoryPalette.muted)
   }
   Box {
    IconButton(onClick={menu=true}) { Icon(Icons.Outlined.Settings,"الإعدادات") }
    DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
     DropdownMenuItem(text={Text("تسجيل الخروج")},onClick={menu=false;onLogout()})
    }
   }
  }
 }
}
@Composable private fun CategoryTicker() {
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
  Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFFE0F2FE)) {
   Row(Modifier.padding(5.dp),verticalAlignment=Alignment.CenterVertically) {
    androidx.compose.foundation.Image(painterResource(R.drawable.ic_smart_cost_logo),"Smart Cost Pro",Modifier.size(34.dp),contentScale=ContentScale.Fit)
    Icon(Icons.Default.ArrowForward,null,tint=CategoryPalette.blue,modifier=Modifier.size(15.dp))
   }
  }
  Spacer(Modifier.width(7.dp))
  Surface(Modifier.weight(1f),shape=RoundedCornerShape(14.dp),color=CategoryPalette.white,border=BorderStroke(1.dp,CategoryPalette.border)) {
   Text("✦ فائق يعزز إنجاز كل مسؤولياتك المالية • تابع تحديثات قسم التكاليف • ",Modifier.fillMaxWidth().padding(10.dp).basicMarquee(iterations=Int.MAX_VALUE,velocity=35.dp),maxLines=1,fontSize=11.sp,color=CategoryPalette.ink)
  }
 }
}
